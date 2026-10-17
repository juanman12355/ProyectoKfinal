package com.example.proyecto_desarrollo.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.proyecto_desarrollo.R
import com.example.proyecto_desarrollo.data.model.Task
import com.example.proyecto_desarrollo.databinding.FragmentTaskListBinding
import com.example.proyecto_desarrollo.ui.auth.AuthViewModel
import com.example.proyecto_desarrollo.ui.form.TaskFormFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()
    private val viewModel: TaskListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = TaskAdapter(
            onItemClick = { task ->
                val args = Bundle().apply {
                    putLong(TaskFormFragment.ARG_TASK_ID, task.id)
                }
                findNavController().navigate(R.id.action_taskList_to_detail, args)
            },
            onToggleDone = viewModel::toggleDone,
            onDeleteClick = ::confirmDelete
        )
        binding.recyclerTasks.adapter = adapter

        binding.toolbar.inflateMenu(R.menu.menu_task_list)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    authViewModel.logout()
                    findNavController().navigate(R.id.action_taskList_to_login)
                    true
                }
                else -> false
            }
        }

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_taskList_to_form)
        }

        // Mensajes de éxito que llegan desde el formulario y el detalle
        setFragmentResultListener(TaskFormFragment.RESULT_TASK_SAVED) { _, _ ->
            Snackbar.make(binding.root, R.string.task_created, Snackbar.LENGTH_SHORT).show()
        }
        setFragmentResultListener(TaskFormFragment.RESULT_TASK_DELETED) { _, _ ->
            Snackbar.make(binding.root, R.string.task_deleted, Snackbar.LENGTH_SHORT).show()
        }

        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            adapter.submitList(tasks)
            binding.emptyState.isVisible = tasks.isEmpty()
            binding.recyclerTasks.isVisible = tasks.isNotEmpty()
        }

        viewModel.error.observe(viewLifecycleOwner) { messageRes ->
            if (messageRes != null) {
                Snackbar.make(binding.root, messageRes, Snackbar.LENGTH_LONG).show()
                viewModel.onErrorHandled()
            }
        }
    }

    private fun confirmDelete(task: Task) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_title)
            .setMessage(getString(R.string.delete_message, task.title))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete_confirm) { _, _ ->
                viewModel.delete(task)
                Snackbar.make(binding.root, R.string.task_deleted, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo) { viewModel.restore(task) }
                    .show()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}