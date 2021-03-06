package loremipsumvirtualenterprise.quest.account

import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import loremipsumvirtualenterprise.quest.R
import loremipsumvirtualenterprise.quest.generic.QuestGenericProgress
import loremipsumvirtualenterprise.quest.main.MainActivity
import loremipsumvirtualenterprise.quest.util.SharedPreferencesHelper

class LoginActivity : AppCompatActivity(), View.OnClickListener
{

    //Firebase references
    private var firebaseAuth: FirebaseAuth? = null

    //region Companion Object
    companion object {
        fun getActivityIntent(context : Context) : Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }

    //endregion

    //region Overridden Methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initViews()
        setUpToolbar()
        changeStatusBarColor()
        initializeVariables()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.loginLoginButton -> {
                doLogin()
            }
            R.id.loginForgotPasswordButton -> {
                val intent : Intent = ForgotPasswordActivity.getActivityIntent(this)
                startActivity(intent)
            }
            R.id.loginNoAccountButton -> {
                val intent : Intent = RegisterActivity.getActivityIntent(this)
                startActivity(intent)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        onBackPressed()
        return true
    }

    //endregion

    //region Private Methods

    private fun initializeVariables() {
        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun initViews() {
        loginPasswordEditText.typeface = ResourcesCompat.getFont(this, R.font.avenir_next_regular)

        loginLoginButton.setOnClickListener(this)
        loginForgotPasswordButton.setOnClickListener(this)
        loginNoAccountButton.setOnClickListener(this)

        mProgress = QuestGenericProgress(this)
    }

    private fun setUpToolbar() {
        setSupportActionBar(mainToolbar as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    private fun changeStatusBarColor() {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorBackground)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decor = getWindow().decorView
            decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    //endregion

    private var mProgress: QuestGenericProgress? = null

    // Actions
    private fun doLogin() {
        if (areFieldsValid()){
            val email = loginLoginEditText!!.text.toString()
            val password = loginPasswordEditText!!.text.toString()

            mProgress?.show()

            firebaseAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener { task: Task<AuthResult> ->

                mProgress?.hide()

                if (task.isSuccessful) {
                    SharedPreferencesHelper.saveStringInSharedPreferences(this, SharedPreferencesHelper.USER_LOGGED, "YES")
                    val mainActivityIntent : Intent = MainActivity.getActivityIntent(this)
                    startActivity(mainActivityIntent)
                    OnboardingActivity.instance?.finish()
                    finish()
                } else {
                    val message = task.exception?.getLocalizedMessage()
                    Toast.makeText(this@LoginActivity, "Houve um erro ao logar o usuário.\n" + message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Validation
    private fun areFieldsValid(): Boolean {
        val isValidEmail = isValidEmail(loginLoginEditText?.text.toString())
        val isValidPassword =  isValidPassword(loginPasswordEditText?.text.toString())
        return isValidEmail && isValidPassword
    }


    private fun isValidEmail(email: String): Boolean {
        if (TextUtils.isEmpty(email)) {
            loginLoginEditText?.error = "E-Mail inválido."
            return false
        }
        loginLoginEditText?.error = null
        return true
    }

    private fun isValidPassword(password: String): Boolean {
        if (TextUtils.isEmpty(password)) {
            loginPasswordEditText?.error = "Senha inválida."
            return false
        } else if(password.length < 6) {
            loginPasswordEditText?.error = "A senha precisa ter no mínimo 6 caracteres."
            return false
        }
        loginPasswordEditText?.error = null
        return true
    }
}
