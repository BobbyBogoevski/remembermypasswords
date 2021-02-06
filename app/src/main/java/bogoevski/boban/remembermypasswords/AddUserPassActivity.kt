package bogoevski.boban.remembermypasswords

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class AddUserPassActivity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_user_pass_layout)
        val btn = findViewById<Button>(R.id.btnAddData)

        btn.setOnClickListener {
            var intent: Intent = Intent()
            val appname = findViewById<EditText>(R.id.textAppName).text.toString()
            val username = findViewById<EditText>(R.id.textUserName).text.toString()
            val password = findViewById<EditText>(R.id.textPassword).text.toString()
            val extra = findViewById<EditText>(R.id.textExtra).text.toString()

            if(appname!=""&&username!=""&&password!=""){
                intent.putExtra("appname",appname)
                intent.putExtra("username",username)
                intent.putExtra("password",password)
                intent.putExtra("extra",extra)
                setResult(Activity.RESULT_OK,intent)
                finish()
            }
            else{
                Toast.makeText(this@AddUserPassActivity,"Empty fields, please fill them before proceeding.",Toast.LENGTH_LONG).show()

            }


        }
    }

}