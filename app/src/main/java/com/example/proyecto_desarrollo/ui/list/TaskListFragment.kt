package com.example.proyecto_desarrollo.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.proyecto_desarrollo.R
import com.example.proyecto_desarrollo.data.model.Task
import com.example.proyecto_desarrollo.databinding.FragmentTaskListBinding
import com.example.proyecto_desarrollo.ui.auth.AuthViewModel
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
            onItemClick = { /* el detalle se conecta en el siguiente paso */ },
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

        // TEMPORAL: en el siguiente paso navegará al formulario de crear
        binding.fabAdd.setOnClickListener { viewModel.addSampleTask() }

        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            adapter.submitList(tasks)
            binding.emptyState.isVisible = tasks.isEmpty()
            binding.recyclerTasks.isVisible = tasks.isNotEmpty()
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