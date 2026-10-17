package com.example.proyecto_desarrollo.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.proyecto_desarrollo.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar
import androidx.core.widget.doOnTextChanged

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.clearErrors()

        binding.etName.doOnTextChanged { text, _, _, _ ->
            viewModel.onNameChanged(text?.toString().orEmpty())
        }
        binding.etEmail.doOnTextChanged { text, _, _, _ ->
            viewModel.onEmailChanged(text?.toString().orEmpty())
        }
        binding.etPassword.doOnTextChanged { text, _, _, _ ->
            // También revalida la confirmación, porque su error depende de este campo
            viewModel.onPasswordChanged(
                text?.toString().orEmpty(),
                binding.etConfirm.text.toString()
            )
        }
        binding.etConfirm.doOnTextChanged { text, _, _, _ ->
            viewModel.onConfirmChanged(
                binding.etPassword.text.toString(),
                text?.toString().orEmpty()
            )
        }

        binding.btnRegister.setOnClickListener {
            viewModel.register(
                binding.etName.text.toString(),
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString(),
                binding.etConfirm.text.toString()
            )
        }
        binding.btnGoLogin.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.formErrors.observe(viewLifecycleOwner) { errors ->
            binding.tilName.error = errors.name?.let { getString(it) }
            binding.tilEmail.error = errors.email?.let { getString(it) }
            binding.tilPassword.error = errors.password?.let { getString(it) }
            binding.tilConfirm.error = errors.confirm?.let { getString(it) }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            val loading = state is AuthState.Loading
            binding.progress.isVisible = loading
            binding.btnRegister.isEnabled = !loading

            when (state) {
                // No lo "consumimos": el login lo verá y mostrará el mensaje de éxito
                is AuthState.Registered -> findNavController().navigateUp()
                is AuthState.Error -> {
                    Snackbar.make(binding.root, state.messageRes, Snackbar.LENGTH_LONG).show()
                    viewModel.onStateHandled()
                }
                else -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}