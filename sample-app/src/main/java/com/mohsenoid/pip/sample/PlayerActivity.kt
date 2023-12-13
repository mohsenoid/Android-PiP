package com.mohsenoid.pip.sample

import android.app.PendingIntent
import android.app.RemoteAction
import android.content.Intent
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.mohsenoid.pip.core.Pip
import com.mohsenoid.pip.core.PipEnterError
import com.mohsenoid.pip.core.PipSupportAppCompatActivity
import com.mohsenoid.pip.sample.databinding.ActivityPlayerBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlayerActivity : PipSupportAppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding

    private val pipCommander = Pip.getPipCommander()
    private val pipObservable = Pip.getPipObservable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupPipButton()

        updatePipActions(listOfNotNull(getPipAction()))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            @Suppress("MagicNumber")
            updatePipRatio(Rational(16, 9))
        }

        binding.buttonContainer.init()
        binding.titleContainer.init()

        lifecycleScope.launch {
            pipObservable.isInPipModeStateFlow.collectLatest { isInPipMode ->
                binding.player.useController = !isInPipMode
            }
        }
    }

    private fun setupPipButton() {
        lifecycleScope.launch {
            pipObservable.isPipAllowedStateFlow.collectLatest {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    binding.button.isVisible = it
                }
            }
        }
        binding.button.setOnClickListener {
            pipCommander.enterPip(
                { alreadyInPipMode ->
                    val successMessage = if (alreadyInPipMode) {
                        "Already in PiP mode"
                    } else {
                        "Entered PiP mode"
                    }
                    Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show()
                },
                { pipEnterError ->
                    val errorMessage = when (pipEnterError) {
                        is PipEnterError.NotSupported -> "PiP is not supported on this device"
                        is PipEnterError.DisabledByUser -> "PiP is disabled by user"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                },
            )
        }
    }

    override fun onStart() {
        super.onStart()
        setupPlayer()
    }

    private fun setupPlayer() {
        val videoUri: Uri =
            Uri.parse("android.resource://" + packageName.toString() + "/" + R.raw.dcbln22)
        val mediaItem: MediaItem = MediaItem.fromUri(videoUri)

        val exoPlayer = ExoPlayer.Builder(this).build().apply {
            setMediaItem(mediaItem)
            prepare()
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
            addListener(
                object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        super.onIsPlayingChanged(isPlaying)
                        updatePlayerState(isPlaying)

                        val rect = Rect()
                        binding.player.getGlobalVisibleRect(rect)
                        updatePipRect(rect)
                    }
                },
            )
        }

        binding.player.player = exoPlayer

        binding.title.setText(R.string.video_title)
    }

    private fun getPipAction(): RemoteAction? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val actionUri = Uri.parse("https://youtube.com/AndroidDeveloperTips")
            val actionIntent = Intent(Intent.ACTION_VIEW, actionUri)
            val actionPendingIntent =
                PendingIntent.getActivity(this, 0, actionIntent, PendingIntent.FLAG_IMMUTABLE)
            val remoteAction = RemoteAction(
                Icon.createWithResource(this, R.drawable.ic_picture_in_picture_action),
                "More info",
                "More info action",
                actionPendingIntent,
            )
            remoteAction
        } else {
            null
        }
    }

    override fun onResume() {
        super.onResume()
        binding.player.player?.play()
    }

    override fun onPause() {
        super.onPause()
        val isInPipMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            isInPictureInPictureMode
        } else {
            false
        }
        if (!isInPipMode) {
            binding.player.player?.pause()
        }
    }

    override fun onStop() {
        super.onStop()
        binding.player.player?.run {
            stop()
            release()
        }
    }
}
