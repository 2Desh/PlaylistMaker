package com.practicum.playlistmaker.presentation.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentCreatePlaylistBinding
import com.practicum.playlistmaker.presentation.utils.setDebouncedOnClickListener
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream

// Экран создания плейлиста
class PlaylistCreateFragment : Fragment() {

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<PlaylistCreateViewModel>()

    private var selectedUri: Uri? = null

    // Счетчик отказов в разрешении
    private var permissionDeniedCount = 0

    // Запуск выбора фото
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedUri = uri
            binding.coverImageView.setImageURI(uri)
        }
    }

    // Запуск системного окна разрешений
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchPicker()
        } else {
            permissionDeniedCount++
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                checkUnsavedDataAndExit()
            }
        })

        updateInputLayoutColor(binding.nameInputLayout, false)
        updateInputLayoutColor(binding.descriptionInputLayout, false)
        setupTextInputsHintColor()

        // Логика текстовых полей и кнопок
        binding.nameEditText.doOnTextChanged { text, _, _, _ ->
            val isNameFilled = !text.isNullOrBlank()

            // Кнопка
            binding.createButton.isEnabled = isNameFilled
            val btnColorRes = if (isNameFilled) R.color.YP_Blue else R.color.YP_Text_Gray
            binding.createButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), btnColorRes)

            // Обводка
            updateInputLayoutColor(binding.nameInputLayout, isNameFilled)
            updateInputLayoutColor(binding.descriptionInputLayout, isNameFilled)
        }

        binding.descriptionEditText.doOnTextChanged { text, _, _, _ ->
            val isDescFilled = !text.isNullOrBlank()
            updateInputLayoutColor(binding.descriptionInputLayout, isDescFilled)
        }

        // Клик по картинке (Запрос разрешений)
        binding.coverImageView.setDebouncedOnClickListener {
            checkAndRequestImagePermission()
        }

        // Кнопка Назад
        binding.backButton.setOnClickListener {
            checkUnsavedDataAndExit()
        }

        // Кнопка Создать
        binding.createButton.setDebouncedOnClickListener {
            val name = binding.nameEditText.text.toString()
            val description = binding.descriptionEditText.text.toString()

            val imagePath = selectedUri?.let { saveImageToPrivateStorage(it) }

            viewModel.createPlaylist(name, description, imagePath)

            Toast.makeText(requireContext(), "Плейлист $name создан", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun updateInputLayoutColor(inputLayout: com.google.android.material.textfield.TextInputLayout, isFilled: Boolean) {
        val colorRes = if (isFilled) R.color.YP_Blue else R.color.edit_text_stroke_color
        val targetColor = ContextCompat.getColor(requireContext(), colorRes)

        val states = arrayOf(
            intArrayOf(android.R.attr.state_focused),
            intArrayOf(-android.R.attr.state_focused)
        )
        val colors = intArrayOf(targetColor, targetColor)

        val colorStateList = ColorStateList(states, colors)

        inputLayout.setBoxStrokeColorStateList(colorStateList)
        inputLayout.boxStrokeColor = targetColor
    }

    private fun setupTextInputsHintColor() {
        val typedValue = android.util.TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
        val defaultHintColor = ColorStateList.valueOf(typedValue.data)

        binding.nameInputLayout.defaultHintTextColor = defaultHintColor
        binding.descriptionInputLayout.defaultHintTextColor = defaultHintColor
    }


    // Проверка разрешений на доступ к фото

    private fun checkAndRequestImagePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val isGranted = ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            launchPicker()
        } else {
            if (permissionDeniedCount >= 3) {
                // После 3 отказов перенаправляем в настройки
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            } else {
                // Диалог
                MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialogTheme)
                    .setTitle("Разрешить приложению «Playlist Maker» доступ к фото?")
                    .setNegativeButton("Нет") { _, _ -> permissionDeniedCount++ }
                    .setPositiveButton("Да") { _, _ -> requestPermissionLauncher.launch(permission) }
                    .show()
            }
        }
    }

    private fun launchPicker() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun saveImageToPrivateStorage(uri: Uri): String {
        val filePath = File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "myalbum")
        if (!filePath.exists()) {
            filePath.mkdirs()
        }

        val file = File(filePath, "cover_${System.currentTimeMillis()}.jpg")
        val inputStream = requireActivity().contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)

        BitmapFactory
            .decodeStream(inputStream)
            ?.compress(Bitmap.CompressFormat.JPEG, 30, outputStream)

        return file.absolutePath
    }

    private fun checkUnsavedDataAndExit() {
        val name = binding.nameEditText.text?.toString().orEmpty()
        val description = binding.descriptionEditText.text?.toString().orEmpty()

        if (selectedUri != null || name.isNotEmpty() || description.isNotEmpty()) {
            MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialogTheme)
                .setTitle("Завершить создание плейлиста?")
                .setMessage("Все несохраненные данные будут потеряны")
                .setNeutralButton("Отмена") { _, _ -> }
                .setPositiveButton("Завершить") { _, _ -> findNavController().navigateUp() }
                .show()
        } else {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}