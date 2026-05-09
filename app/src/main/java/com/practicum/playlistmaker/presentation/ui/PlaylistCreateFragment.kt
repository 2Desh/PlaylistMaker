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
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentCreatePlaylistBinding
import com.practicum.playlistmaker.domain.models.Playlist
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

    private var editablePlaylist: Playlist? = null

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

        initEditablePlaylist()
        setupBackNavigation()
        setupTextWatchers()
        setupClickListeners()
        observeViewModel()
    }

    private fun initEditablePlaylist() {
        // Получаем плейлист, если мы вернулись с экрана редактирования
        editablePlaylist = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("playlist", Playlist::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable("playlist") as? Playlist
        }

        if (editablePlaylist != null) {
            setupEditMode()
        }
    }

    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                checkUnsavedDataAndExit()
            }
        })
    }

    private fun setupTextWatchers() {
        updateInputLayoutColor(binding.nameInputLayout, !binding.nameEditText.text.isNullOrBlank())
        updateInputLayoutColor(binding.descriptionInputLayout, !binding.descriptionEditText.text.isNullOrBlank())
        setupTextInputsHintColor()

        // Логика текстовых полей и кнопок
        binding.nameEditText.doOnTextChanged { text, _, _, _ ->
            val isNameFilled = !text.isNullOrBlank()

            // Кнопка
            binding.createButton.isEnabled = isNameFilled
            val btnColorRes = if (isNameFilled) R.color.YP_Blue else R.color.YP_Text_Gray
            binding.createButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), btnColorRes)
            updateInputLayoutColor(binding.nameInputLayout, isNameFilled)
            updateInputLayoutColor(binding.descriptionInputLayout, !binding.descriptionEditText.text.isNullOrBlank())
        }

        binding.descriptionEditText.doOnTextChanged { text, _, _, _ ->
            val isDescFilled = !text.isNullOrBlank()
            updateInputLayoutColor(binding.descriptionInputLayout, isDescFilled)
        }
    }

    private fun setupClickListeners() {
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

            // Если новая картинка не выбрана, сохраняем старую
            val imagePath = if (selectedUri != null) {
                saveImageToPrivateStorage(selectedUri!!)
            } else {
                editablePlaylist?.coverFilePath
            }

            if (editablePlaylist == null) {
                // Режим создания
                viewModel.createPlaylist(name, description, imagePath)
            } else {
                // Режим редактирования
                viewModel.updatePlaylist(
                    id = editablePlaylist!!.id,
                    name = name,
                    description = description,
                    coverFilePath = imagePath,
                    trackIds = editablePlaylist!!.trackIds,
                    trackCount = editablePlaylist!!.trackCount
                )
            }
        }
    }

    private fun observeViewModel() {
        // Подписка на успешное сохранение плейлиста
        viewModel.isSaved.observe(viewLifecycleOwner, Observer { isSaved ->
            if (isSaved) {
                if (editablePlaylist == null) {
                    val name = binding.nameEditText.text.toString()
                    val message = getString(R.string.playlist_created, name)
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
                findNavController().navigateUp()
            }
        })
    }

    private fun setupEditMode() {
        // Меняем заголовок и кнопку для режима редактирования
        binding.screenTitle.text = "Редактировать"
        binding.createButton.text = "Сохранить"

        // Подставляем существующие данные
        binding.nameEditText.setText(editablePlaylist?.name)
        binding.descriptionEditText.setText(editablePlaylist?.description)

        // Загружаем обложку
        val coverPath = editablePlaylist?.coverFilePath
        if (!coverPath.isNullOrEmpty()) {
            val file = File(coverPath)
            if (file.exists()) {
                Glide.with(this)
                    .load(file)
                    .transform(
                        CenterCrop(),
                        RoundedCorners(resources.getDimensionPixelSize(R.dimen.cover_corner_radius))
                    )
                    .into(binding.coverImageView)
            }
        }
    }

    private fun checkUnsavedDataAndExit() {
        if (editablePlaylist != null) {
            findNavController().navigateUp()
            return
        }

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

    private fun saveImageToPrivateStorage(uri: Uri): String? {
        val filePath = File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "myalbum")
        if (!filePath.exists()) {
            filePath.mkdirs()
        }

        val file = File(filePath, "cover_${System.currentTimeMillis()}.jpg")

        return try {
            requireActivity().contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    BitmapFactory
                        .decodeStream(inputStream)
                        ?.compress(Bitmap.CompressFormat.JPEG, 30, outputStream)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}