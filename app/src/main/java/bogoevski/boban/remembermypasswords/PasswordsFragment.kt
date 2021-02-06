package bogoevski.boban.remembermypasswords

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class PasswordsFragment: Fragment() {
    lateinit var listener:IFragmentListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.password_layout,container,false)
    }

    fun setFragmentListener(listener: IFragmentListener){
        this.listener=listener
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
            listener.fragmentInitialized()

    }
}