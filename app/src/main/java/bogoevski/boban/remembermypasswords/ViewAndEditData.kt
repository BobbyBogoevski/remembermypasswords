package bogoevski.boban.remembermypasswords

import android.content.DialogInterface
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.StringBuilder
import java.nio.charset.Charset
import kotlin.experimental.xor

class ViewAndEditData:AppCompatActivity() {


    lateinit var dc:CollectionReference
    lateinit var appname_text:EditText
    lateinit var username_text:EditText
    lateinit var password_text:EditText
    lateinit var extra_text:EditText
    lateinit var id:String
    val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_and_edit_layout)
        dc = FirebaseFirestore.getInstance().collection("users").document(encode(auth.currentUser!!.email!!)).collection("passwords")
        id=intent.getStringExtra("id")
      dc.document(id).get().addOnSuccessListener {
           val test = it.data!!
           appname_text= findViewById<EditText>(R.id.vne_appname)
           username_text=findViewById<EditText>(R.id.vne_username)
           password_text=findViewById<EditText>(R.id.vne_password)
           extra_text=findViewById<EditText>(R.id.vne_extra)

           appname_text.setText(decode(test["appname"].toString()))
            username_text.setText(decode(test["username"].toString()))
           password_text.setText(decode(test["password"].toString()))
           extra_text.setText(decode(test["extra"].toString()))
       }



    }

    private fun decode(str: String): String {
        val key = auth.currentUser!!.uid.toByteArray()
        val res=StringBuilder()
        for(i in 0..str.length-1){
            res.append(((str[i]-65).toByte() xor key[i%str.length]).toChar())
        }
        return res.toString()
    }

    fun updateData(v: View){

        val obj = mapOf<String,String>(
            "appname" to encode(appname_text.text.toString()),
            "username" to encode(username_text.text.toString()),
            "password" to encode(password_text.text.toString()),
            "extra" to encode(extra_text.text.toString())

        )
        dc.document(id).update(obj)
        finish()

    }

    private fun encode(str: String): String {

        val key = auth.currentUser!!.uid.toByteArray(Charset.defaultCharset())
        val res= StringBuilder()
        for(i in 0..str.length-1){
            res.append((str[i].toByte() xor key[(i%str.length)]).toChar()+65)
        }
        return res.toString()
    }

    fun deleteData(v:View){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Delete Data")
            .setMessage("Are you sure you want to delete this data?")
            .setPositiveButton("Yes",object:DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dc.document(id).delete()
                    finish()
                }

            })
            .setNegativeButton("No",object : DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {

                }

            })

        dialog.create().show()


    }

}