package com.hom8.app.presentation.onboarding

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hom8.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpStepFragment : Fragment() {

    private val viewModel: OnboardingViewModel by viewModels({ requireParentFragment() })
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { token ->
                    Log.d("SignUpStepFragment", "Google Sign-In exitoso, token obtenido")
                    viewModel.signInWithGoogle(token)
                } ?: run {
                    Log.e("SignUpStepFragment", "No se pudo obtener el idToken")
                    Snackbar.make(
                        requireView(),
                        "Error al obtener credenciales de Google",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } catch (e: ApiException) {
                Log.e("SignUpStepFragment", "Error en Google Sign-In: ${e.statusCode}", e)
                Snackbar.make(
                    requireView(),
                    "Error al iniciar sesión con Google: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        } else {
            Log.d("SignUpStepFragment", "Google Sign-In cancelado por el usuario")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configurar Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_onboarding_signup, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName = view.findViewById<TextInputEditText>(R.id.etName)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmailSignUp)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPasswordSignUp)
        val tilName = view.findViewById<TextInputLayout>(R.id.tilName)
        val tilEmail = view.findViewById<TextInputLayout>(R.id.tilEmailSignUp)
        val tilPassword = view.findViewById<TextInputLayout>(R.id.tilPasswordSignUp)
        val tvError = view.findViewById<TextView>(R.id.tvSignUpError)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressSignUp)
        val btnCreate = view.findViewById<MaterialButton>(R.id.btnCreateAccount)
        val btnGoogleSignUp = view.findViewById<MaterialButton>(R.id.btnGoogleSignUp)

        // Back → Welcome
        view.findViewById<ImageButton>(R.id.btnBackSignUp).setOnClickListener {
            (parentFragment as? OnboardingFragment)?.navigateToStep(0)
        }

        // Create account
        btnCreate.setOnClickListener {
            val name = etName.text?.toString()?.trim() ?: ""
            val email = etEmail.text?.toString()?.trim() ?: ""
            val password = etPassword.text?.toString() ?: ""

            var valid = true
            if (name.isBlank()) {
                tilName.error = "El nombre es obligatorio"
                valid = false
            } else tilName.error = null

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Ingresa un correo electrónico válido"
                valid = false
            } else tilEmail.error = null

            if (password.length < 6) {
                tilPassword.error = "La contraseña debe tener al menos 6 caracteres"
                valid = false
            } else tilPassword.error = null

            if (valid) viewModel.signUpWithEmail(email, password, name)
        }

        // Google Sign-In
        btnGoogleSignUp.setOnClickListener {
            Log.d("SignUpStepFragment", "Iniciando Google Sign-In")
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        // Already have account → Sign In
        view.findViewById<TextView>(R.id.tvGoToSignIn).setOnClickListener {
            (parentFragment as? OnboardingFragment)?.navigateToStep(1)
        }

        // Observe state
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    btnCreate.isEnabled = !state.isLoading
                    btnGoogleSignUp.isEnabled = !state.isLoading

                    if (state.error != null) {
                        tvError.text = state.error
                        tvError.visibility = View.VISIBLE
                        Snackbar.make(requireView(), state.error, Snackbar.LENGTH_LONG).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }
}
