package com.example.unchilnote.data.network

import android.content.res.Resources
import com.example.unchilnote.R
import com.example.unchilnote.utils.SPEECHTOTEXT_LANGUAGE
import com.example.unchilnote.utils.SPEECHTOTEXT_SAMPLERATE
import com.google.api.gax.longrunning.OperationFuture
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.speech.v1.*
import com.google.protobuf.ByteString
import java.nio.file.Files
import java.nio.file.Paths

class GoogleSpeechToTextService (val resources: Resources) {
    private val logTag = GoogleSpeechToTextService::class.java.name

    fun asyncRecognizeFile(fileName: String): String {
        try {
            val speechClient : SpeechClient

            resources.openRawResource(R.raw.credential).use {
                speechClient = SpeechClient.create(
                    SpeechSettings.newBuilder()
                    .setCredentialsProvider{ GoogleCredentials.fromStream(it)}.build()
                )
            }

            val audioFileByteString = ByteString.copyFrom(Files.readAllBytes(Paths.get(fileName)))

            val config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.OGG_OPUS)
                .setLanguageCode(SPEECHTOTEXT_LANGUAGE)
                .setSampleRateHertz(SPEECHTOTEXT_SAMPLERATE)
                .build()

            val audio = RecognitionAudio.newBuilder().setContent(audioFileByteString).build()

            val response: OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata>
                    = speechClient.longRunningRecognizeAsync(config, audio)

            val results: List<SpeechRecognitionResult> = response.get().resultsList

            var encText = ""
            for ( result in results) {
                encText += result.alternativesList[0].transcript
                encText += " "
            }

            return encText

        } catch ( e: Exception) {

            return e.message!!
        }
    }

}