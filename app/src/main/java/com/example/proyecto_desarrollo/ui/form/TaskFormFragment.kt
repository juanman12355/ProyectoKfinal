package com.example.proyecto_desarrollo.ui.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.proyecto_desarrollo.R
import com.example.proyecto_desarrollo.databinding.FragmentTaskFormBinding
import androidx.core.widget.doOnTextChanged

class TaskFormFragment : Fragment() {

    private var _binding: FragmentTaskFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskFormViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taskId = arguments?.getLong(ARG_TASK_ID, NO_TASK) ?: NO_TASK
        val isEdit = taskId != NO_TASK

        binding.toolbar.setTitle(if (isEdit) R.string.form_edit_title else R.string.form_new_title)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        if (isEdit) {
            viewModel.load(taskId)
            viewModel.loadedTask.observe(viewLifecycleOwner) { task ->
                // Solo rellenamos la primera vez; al rotar, Android restaura lo que el usuario escribió
                if (task != null && savedInstanceState == null) {
                    binding.etTitle.setText(task.title)
                    binding.etDescription.setText(task.description)
                }
            }
        }

        binding.etTitle.doOnTextChanged { text, _, _, _ ->
            viewModel.onTitleChanged(text?.toString().orEmpty())
        }
        binding.etDescription.doOnTextChanged { text, _, _, _ ->
            viewModel.onDescriptionChanged(text?.toString().orEmpty())
        }

        binding.btnSave.setOnClickListener {
            viewModel.save(
                binding.etTitle.text.toString(),
                binding.etDescription.text.toString()
            )
        }

        viewModel.errors.observe(viewLifecycleOwner) { errors ->
            binding.tilTitle.error = errors.title?.let { getString(it) }
            binding.tilDescription.error = errors.description?.let { getString(it) }
        }

        viewModel.saved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                // Avisa a la pantalla anterior para que muestre el mensaje de éxito
                setFragmentResult(RESULT_TASK_SAVED, Bundle())
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_TASK_ID = "taskId"
        const val NO_TASK = -1L
        const val RESULT_TASK_SAVED = "task_saved"
        const val RESULT_TASK_DELETED = "task_deleted"
    }
}