package bogoevski.boban.remembermypasswords

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import java.io.File


class FileRecyclerViewAdapter(val files:ArrayList<FirebaseFiles>):RecyclerView.Adapter<FileRecyclerViewAdapter.FilesViewHolder>() {
    lateinit var c: Context
    val storageRef= FirebaseStorage.getInstance().reference
    lateinit var activity: MainActivity

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): FilesViewHolder {
     val cardView = LayoutInflater.from(p0.context).inflate(R.layout.file_card_view,p0,false) as androidx.cardview.widget.CardView
        return  FilesViewHolder(cardView)
    }

    fun bindActivity(activity: MainActivity){
        this.activity=activity
    }
    
    fun bindContext(context: Context){
       c = context
    }
    override fun onBindViewHolder(p0: FilesViewHolder, p1: Int) {
       val filename_textview = p0.cardView.findViewById<TextView>(R.id.fcv_filename)
       val image_view = p0.cardView.findViewById<ImageView>(R.id.fcv_image)
        filename_textview.text = files[p1].filename
        val type = files[p1].type.split("/")[0]
        if(type.equals("image")){
            GlideApp.with(c).load(storageRef.child(files[p1].path)).placeholder(ContextCompat.getDrawable(c,R.drawable.loading)).into(image_view)
        }
        else if(type.equals("audio")){
            val img = ContextCompat.getDrawable(c,R.drawable.ic_music_note_black_24dp)
            GlideApp.with(c).load(img).into(image_view)
        }
        else if(type.equals("video")){
            val img = ContextCompat.getDrawable(c,R.drawable.ic_videocam_black_24dp)
            GlideApp.with(c).load(img).into(image_view)
        }
        else
        {
            val img = ContextCompat.getDrawable(c,R.drawable.ic_insert_drive_file_black_24dp)
            GlideApp.with(c).load(img).into(image_view)
        }

        val btnDelete= p0.cardView.findViewById<Button>(R.id.fcv_delete)
        val btnDownload = p0.cardView.findViewById<Button>(R.id.fcv_download)

        btnDelete.setOnClickListener{

            val dialog = AlertDialog.Builder(c)
            dialog.setTitle("Delete File")
            dialog.setMessage("Are you sure you want to delete this file?")
            dialog.setPositiveButton("Yes",object:DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {
                val success = activity.deleteFile(files[p1])
                    if(success){
                        files.removeAt(p1)
                    }
                }

            })
            dialog.setNegativeButton("No",object:DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {

                }
            })

            val alert = dialog.create()
            alert.show()
        }

        btnDownload.setOnClickListener {
            val fileRef = storageRef.child(files[p1].path)
            val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            var file = File(root.absolutePath,files[p1].filename)
            val file_string = files[p1].filename.split(".")
//            val file = File.createTempFile(file_string[0],"."+file_string[1],root)
            var count = 1
            while(file.exists()){
                file = File(root.absolutePath,file_string[0]+"_rmp"+count+"."+file_string[1])
                count++
            }

            file.createNewFile()

            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O)
            {
                val notificationChannel = NotificationChannel(files[p1].filename,files[p1].filename, NotificationManager.IMPORTANCE_LOW)
                val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(notificationChannel)
            }

            val notification = NotificationCompat.Builder(c,file.name).setSmallIcon(R.drawable.ic_file_download_black_24dp)
            notification.setContentTitle("Downloading "+files[p1].filename)
                .setProgress(100, 0,false)

            val id = file.name.hashCode()
            NotificationManagerCompat.from(c).notify(id,notification.build())


            fileRef.getFile(file).addOnProgressListener {
                val total = it.totalByteCount
                val current = it.bytesTransferred
                val progress= ((100.0*current/total)).toInt()
                if(progress!=100) notification
                    .setProgress(100, progress,false)
                else notification.setContentTitle(files[p1].filename+" successfully downloaded")
                    .setProgress(0, 0,false)
                NotificationManagerCompat.from(c).notify(id,notification.build())
            }
                .addOnSuccessListener {
                    activity.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.fromFile(file)))
              Toast.makeText(c,"Successfully downloaded file as " + file.name,Toast.LENGTH_LONG).show()
            }





            }

        }



    override fun getItemCount(): Int {
      return files.size
    }

    class FilesViewHolder(val cardView: CardView):
        RecyclerView.ViewHolder(cardView)
}