package com.example.proyecto_desarrollo.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.proyecto_desarrollo.R
import com.example.proyecto_desarrollo.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import androidx.core.widget.doOnTextChanged

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Si ya hay sesión guardada, saltamos directo a la lista
        if (viewModel.isLoggedIn()) {
            goToTasks()
            return
        }

        viewModel.clearErrors()

        binding.etEmail.doOnTextChanged { text, _, _, _ ->
            viewModel.onEmailChanged(text?.toString().orEmpty())
        }
        binding.etPassword.doOnTextChanged { text, _, _, _ ->
            viewModel.onPasswordChanged(text?.toString().orEmpty())
        }

        binding.btnLogin.setOnClickListener {
            viewModel.login(
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString()
            )
        }
        binding.btnGoRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        viewModel.formErrors.observe(viewLifecycleOwner) { errors ->
            binding.tilEmail.error = errors.email?.let { getString(it) }
            binding.tilPassword.error = errors.password?.let { getString(it) }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            val loading = state is AuthState.Loading
            binding.progress.isVisible = loading
            binding.btnLogin.isEnabled = !loading

            when (state) {
                is AuthState.LoggedIn -> {
                    viewModel.onStateHandled()
                    goToTasks()
                }
                is AuthState.Registered -> {
                    Snackbar.make(binding.root, R.string.register_success, Snackbar.LENGTH_LONG).show()
                    viewModel.onStateHandled()
                }
                is AuthState.Error -> {
                    Snackbar.make(binding.root, state.messageRes, Snackbar.LENGTH_LONG).show()
                    viewModel.onStateHandled()
                }
                else -> Unit
            }
        }
    }

    private fun goToTasks() {
        findNavController().navigate(R.id.action_login_to_taskList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}