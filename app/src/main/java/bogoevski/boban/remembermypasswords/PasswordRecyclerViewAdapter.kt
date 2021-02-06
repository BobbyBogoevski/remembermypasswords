package bogoevski.boban.remembermypasswords

import android.content.Intent
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

class PasswordRecyclerViewAdapter(val dataset:ArrayList<Data>): RecyclerView.Adapter<PasswordRecyclerViewAdapter.PasswordViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): PasswordViewHolder {
        val cardView = LayoutInflater.from(p0.context).inflate(R.layout.password_card_view,p0,false) as androidx.cardview.widget.CardView
        return  PasswordViewHolder(cardView)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }



    override fun onBindViewHolder(p0: PasswordViewHolder, p1: Int) {
      val appname_textview = p0.cardView.findViewById<TextView>(R.id.cv_appname)
        val username_textview = p0.cardView.findViewById<TextView>(R.id.cv_username)
        val password_textview = p0.cardView.findViewById<TextView>(R.id.cv_password)
        val extra_textview = p0.cardView.findViewById<TextView>(R.id.cv_extra)
        val id_textview=p0.cardView.findViewById<TextView>(R.id.cv_id)
        val btn = p0.cardView.findViewById<Button>(R.id.btnView)
        btn.setOnClickListener {
            val intent = Intent(it.context,ViewAndEditData::class.java)
            intent.putExtra("id",id_textview.text)
            it.context.startActivity(intent)


        }

        appname_textview.setText(dataset[p1].appname)
        username_textview.setText(dataset[p1].username)
        password_textview.setText(dataset[p1].password)

        extra_textview.setText(dataset[p1].extra)
        id_textview.setText(dataset[p1].id)

    }

    class PasswordViewHolder(val cardView: CardView):
        RecyclerView.ViewHolder(cardView)
}