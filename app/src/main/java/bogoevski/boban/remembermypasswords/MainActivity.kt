package bogoevski.boban.remembermypasswords

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import java.lang.StringBuilder
import java.nio.charset.Charset
import java.util.Arrays.asList
import java.util.jar.Manifest
import kotlin.collections.ArrayList
import kotlin.experimental.xor


class MainActivity : AppCompatActivity(),IFragmentListener {



    lateinit var providers: List<AuthUI.IdpConfig>
    lateinit var fragmentManager: FragmentManager
    lateinit var fragmentTransaction: FragmentTransaction
    val auth=FirebaseAuth.getInstance()
    var listener:ListenerRegistration? = null
    val db = FirebaseFirestore.getInstance()
    val storageRef=FirebaseStorage.getInstance().reference
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout
    val SIGN_IN_REQUEST = 1
    val ADD_NEW_DATA=2
    val ADD_NEW_FILE=3
    val REQUEST_WRITE_PERMISSION=4


    override fun fragmentInitialized() {

        drawerLayout = findViewById<DrawerLayout>(R.id.drawer)
        toggle = object : ActionBarDrawerToggle(this@MainActivity, drawerLayout, R.string.open_drawer, R.string.close_drawer){
            override fun onDrawerOpened(drawerView: View) {
                drawerView.findViewById<TextView>(R.id.drawer_header_user).text=auth.currentUser!!.email
                super.onDrawerOpened(drawerView)

            }
        }

        drawerLayout.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setTitle(R.string.menu_passwords)
        toggle.syncState()

        fetchPasswordData()

    }

    fun fetchPasswordData(){
        if(auth.currentUser!=null) {
            if(listener!=null) listener?.remove()
            listener = db.collection("users").document(encode(auth.currentUser!!.email!!))
                .collection("passwords")
                .addSnapshotListener { qs, _ ->
                    updateData(qs!!.documents)
                }
        }
    }

    fun fetchFiles(){
        if(auth.currentUser!=null) {
            if(listener!=null) listener?.remove()
            listener = db.collection("users").document(encode(auth.currentUser!!.email!!))
                .collection("files")
                .addSnapshotListener { qs, _ ->
                    updateData(qs!!.documents)
                }
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragmentManager = supportFragmentManager
        fragmentTransaction = fragmentManager.beginTransaction()

        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),REQUEST_WRITE_PERMISSION)
            }

        }

        providers = asList(
            AuthUI.IdpConfig.EmailBuilder().build()
        )

        var fragment: Fragment
        if (auth.currentUser == null) {
            fragment = AppLoginFragment()
        } else {
            fragment = PasswordsFragment()
            fragment.setFragmentListener(this)

        }
            fragmentTransaction.add(R.id.main_view_Fragment, fragment)

            fragmentTransaction.commit()

        }




    fun encode(str: String):String{
        val key = auth.currentUser!!.uid
        val res=StringBuilder()
        for(i in 0..str.length-1){
            val letter =(str[i].toShort() xor key[(i%key.length)].toShort())+65
            res.append(letter.toChar())
        }
        return res.toString()
    }

    fun decode(str: String):String{
        val key = auth.currentUser!!.uid
        val res=StringBuilder()
        for(i in 0..str.length-1){
            val letter = ((str[i]-65).toShort() xor key[i%key.length].toShort())
            res.append(letter.toChar())
        }
        return res.toString()
    }

     public fun signIn(v: View) {

            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setIsSmartLockEnabled(true,true)
                    .build(),
                SIGN_IN_REQUEST
            )


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.size>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
            Log.d("Permission","Permission: "+ permissions[0] + "was" + grantResults[0] )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==SIGN_IN_REQUEST){

            if(resultCode == Activity.RESULT_OK){
                val fragment=PasswordsFragment()
                showLayout(fragment)
                fragment.setFragmentListener(this)

            }

        }
        else if(requestCode==ADD_NEW_DATA){

            if(resultCode==Activity.RESULT_OK){
                val obj= mapOf<String,String>(
                    "appname" to encode(data!!.getStringExtra("appname")),
                    "username" to encode(data.getStringExtra("username")),
                    "password" to encode(data.getStringExtra("password")),
                    "extra" to encode(data.getStringExtra("extra")),
                    "author" to encode(auth.currentUser!!.email!!)
                    )
                val dc=db.collection("users").document(encode(auth.currentUser!!.email!!)).collection("passwords")
               dc.add(obj)


            }

        }
        else if(requestCode==ADD_NEW_FILE){

            if(resultCode==Activity.RESULT_OK&&data!=null&&data.data!=null){
                val stream = contentResolver.openInputStream(data.data)
                val cursor = contentResolver.query(data.data,null,null,null,null,null)
                cursor.moveToFirst()
                val type = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE))
                val name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                val path = auth.currentUser!!.email!!+"/"+name
                val size = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.SIZE)).toLong()
                val fileRef = storageRef.child(path)
                cursor.close()

                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                    val notificationChannel = NotificationChannel(name,name,NotificationManager.IMPORTANCE_LOW)
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.createNotificationChannel(notificationChannel)
                }

                Toast.makeText(this,"Uploading file",Toast.LENGTH_SHORT).show()
                fileRef.putStream(stream).addOnProgressListener {
                    val current = it.bytesTransferred
                    val progress= ((100.0*current/size)).toInt()
                    val notification = NotificationCompat.Builder(this,name).setSmallIcon(R.drawable.ic_file_upload_black_24dp)

                    if(progress!=100) notification.setContentTitle("Uploading "+name)
                        .setProgress(100, progress,false)
                    else notification.setContentTitle(name+" successfully uploaded")
                        .setProgress(0, 0,false)

                    NotificationManagerCompat.from(this).notify(name.hashCode(),notification.build())

                }.addOnSuccessListener {
                    val file = mapOf<String,String>(
                        "filename" to encode(name),
                        "path" to encode(path),
                        "type" to encode(type)
                    )
                    db.collection("users").document(encode(auth.currentUser!!.email!!)).collection("files").add(file)
                    Toast.makeText(this@MainActivity,"Successfully added file",Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this@MainActivity,"Failed to upload file. "+it.message,Toast.LENGTH_SHORT).show()
                }.addOnCompleteListener {

                }
            }
        }


    }

    private fun updateData(docs: List<DocumentSnapshot>) {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility=View.VISIBLE
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView)
        if(supportActionBar!!.title!!.equals(resources.getString(R.string.menu_passwords))) {

            val dataset = ArrayList<Data>()
            for (doc in docs) {

                val d = Data(
                    doc.id,
                    decode(doc.getString("appname")!!),
                    decode(doc.getString("username")!!),
                    decode(doc.getString("password")!!),
                    decode(doc.getString("extra")!!)
                )
                dataset.add(d)
            }
            recyclerView.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(this@MainActivity)
            val adapter = PasswordRecyclerViewAdapter(dataset)
            recyclerView.adapter = adapter
        }
        else{
            val dataset = ArrayList<FirebaseFiles>()
            for (doc in docs) {

                val d = FirebaseFiles(
                    doc.id,
                    decode(doc.getString("filename")!!),
                    decode(doc.getString("path")!!),
                    decode(doc.getString("type")!!)
                )
                dataset.add(d)
            }
            recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@MainActivity)
            val adapter = FileRecyclerViewAdapter(dataset)
            adapter.bindContext(this@MainActivity)
            adapter.bindActivity(this)
            recyclerView.adapter = adapter
        }


        progressBar.visibility=View.INVISIBLE
    }


    fun addNewData(v:View){

        val password_string = resources.getString(R.string.menu_passwords)
        val files_string = resources.getString(R.string.menu_files)
        if(supportActionBar!!.title!!.equals(password_string)) {
            val intent = Intent(this@MainActivity, AddUserPassActivity::class.java)
            startActivityForResult(intent, ADD_NEW_DATA)
        }
        else if(supportActionBar!!.title!!.equals(files_string)){
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("*/*")
            val chooser = Intent.createChooser(intent,"Select an app")
            startActivityForResult(chooser,ADD_NEW_FILE)
        }
    }

    fun showLayout(fragment: Fragment){
        fragmentTransaction=fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main_view_Fragment,fragment)
        fragmentTransaction.commit()
    }



    fun signOut(item: MenuItem){
        auth.signOut()
        listener?.remove()
        supportActionBar?.setTitle(resources.getString(R.string.app_name))
        val fragment = AppLoginFragment()
        showLayout(fragment)

    }

    fun loadPasswords(item: MenuItem){
        supportActionBar?.setTitle(R.string.menu_passwords)
        fetchPasswordData()
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    fun loadFiles(item: MenuItem){
        supportActionBar?.setTitle(R.string.menu_files)
        fetchFiles()
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    fun deleteFile(file:FirebaseFiles):Boolean{
        var successful = false

        storageRef.child(file.path).delete().addOnSuccessListener {
        db.collection("users").document(encode(auth.currentUser!!.email!!)).collection("files").document(file.id).delete()
            .addOnSuccessListener {
                    successful = true
                }
            }
        return successful
    }

}
