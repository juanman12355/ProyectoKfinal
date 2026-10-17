package com.example.proyecto_desarrollo.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.proyecto_desarrollo.R
import com.example.proyecto_desarrollo.data.model.Task
import com.example.proyecto_desarrollo.databinding.FragmentTaskDetailBinding
import com.example.proyecto_desarrollo.ui.form.TaskFormFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.DateFormat
import java.util.Date

class TaskDetailFragment : Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taskId = arguments?.getLong(TaskFormFragment.ARG_TASK_ID, TaskFormFragment.NO_TASK)
            ?: TaskFormFragment.NO_TASK
        viewModel.load(taskId)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        // El formulario nos avisa cuando se guardó una edición
        setFragmentResultListener(TaskFormFragment.RESULT_TASK_SAVED) { _, _ ->
            Snackbar.make(binding.root, R.string.task_updated, Snackbar.LENGTH_SHORT).show()
        }

        viewModel.task.observe(viewLifecycleOwner) { task ->
            if (task == null) {
                // La tarea ya no existe (p. ej. se acaba de eliminar): volvemos a la lista
                findNavController().navigateUp()
            } else {
                bind(task)
            }
        }

        binding.btnToggleDone.setOnClickListener {
            viewModel.task.value?.let { viewModel.toggleDone(it) }
        }
        binding.btnEdit.setOnClickListener {
            viewModel.task.value?.let {
                findNavController().navigate(
                    R.id.action_detail_to_form,
                    bundleOf(TaskFormFragment.ARG_TASK_ID to it.id)
                )
            }
        }
        binding.btnShare.setOnClickListener { viewModel.task.value?.let(::share) }
        binding.btnDelete.setOnClickListener { viewModel.task.value?.let(::confirmDelete) }
    }

    private fun bind(task: Task) {
        binding.tvTitle.text = task.title
        binding.tvDescription.text = task.description.ifBlank { getString(R.string.no_description) }
        binding.tvStatus.setText(if (task.done) R.string.status_done else R.string.status_pending)
        binding.btnToggleDone.setText(
            if (task.done) R.string.btn_mark_pending else R.string.btn_mark_done
        )
        val date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            .format(Date(task.createdAt))
        binding.tvCreated.text = getString(R.string.created_on, date)
    }

    private fun share(task: Task) {
        val body = if (task.description.isBlank()) task.title
        else "${task.title}\n\n${task.description}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, task.title)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_title)))
    }

    private fun confirmDelete(task: Task) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_title)
            .setMessage(getString(R.string.delete_message, task.title))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete_confirm) { _, _ ->
                // La lista mostrará "Tarea eliminada" cuando volvamos a ella
                setFragmentResult(TaskFormFragment.RESULT_TASK_DELETED, Bundle())
                viewModel.delete(task)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}