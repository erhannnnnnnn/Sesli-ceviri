package com.example.depthlayers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.FloatBuffer
import java.util.Arrays

class DepthLayerProcessor(private val context: Context) {
    private var ortEnvironment: OrtEnvironment? = null
    private var ortSession: OrtSession? = null

    init { initOnnx() }

    private fun initOnnx() {
        ortEnvironment = OrtEnvironment.getEnvironment()
        val modelBytes = context.assets.open("depth_anything_v2_small.onnx").readBytes()
        val sessionOptions = OrtSession.SessionOptions().apply { setIntraOpNumThreads(4) }
        ortSession = ortEnvironment?.createSession(modelBytes, sessionOptions)
    }

    suspend fun processImage(originalBitmap: Bitmap, isPng: Boolean): List<Bitmap> = withContext(Dispatchers.Default) {
        val targetSize = 518
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetSize, targetSize, true)
        val floatBuffer = FloatBuffer.allocate(1 * 3 * targetSize * targetSize)
        val pixels = IntArray(targetSize * targetSize)
        scaledBitmap.getPixels(pixels, 0, targetSize, 0, 0, targetSize, targetSize)

        val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
        val std = floatArrayOf(0.229f, 0.224f, 0.225f)
        for (c in 0..2) {
            for (i in pixels.indices) {
                val pixel = pixels[i]
                val channelVal = when (c) {
                    0 -> Color.red(pixel) / 255.0f
                    1 -> Color.green(pixel) / 255.0f
                    else -> Color.blue(pixel) / 255.0f
                }
                floatBuffer.put((channelVal - mean[c]) / std[c])
            }
        }
        floatBuffer.flip()

        val env = ortEnvironment ?: error("ONNX environment unavailable")
        val session = ortSession ?: error("ONNX session unavailable")
        val inputName = session.inputNames.iterator().next()
        OnnxTensor.createTensor(env, floatBuffer, longArrayOf(1, 3, targetSize.toLong(), targetSize.toLong())).use { tensor ->
            session.run(mapOf(inputName to tensor)).use { output ->
                @Suppress("UNCHECKED_CAST")
                val rawDepth = (output[0].value as Array<Array<FloatArray>>)[0]

                val origW = originalBitmap.width
                val origH = originalBitmap.height
                val fullDepth = FloatArray(origW * origH)
                val sortedDepth = FloatArray(origW * origH)
                var idx = 0
                for (y in 0 until origH) {
                    val srcY = (y * targetSize) / origH
                    for (x in 0 until origW) {
                        val srcX = (x * targetSize) / origW
                        val d = rawDepth[srcY][srcX]
                        fullDepth[idx] = d
                        sortedDepth[idx] = d
                        idx++
                    }
                }

                Arrays.sort(sortedDepth)
                val totalPixels = sortedDepth.size
                val q20 = sortedDepth[(totalPixels * 0.20).toInt()]
                val q40 = sortedDepth[(totalPixels * 0.40).toInt()]
                val q60 = sortedDepth[(totalPixels * 0.60).toInt()]
                val q80 = sortedDepth[(totalPixels * 0.80).toInt()]
                val thresholds = listOf(q80 to Float.MAX_VALUE, q60 to q80, q40 to q60, q20 to q40, -Float.MAX_VALUE to q20)

                val origPixels = IntArray(origW * origH)
                originalBitmap.getPixels(origPixels, 0, origW, 0, 0, origW, origH)
                val layers = mutableListOf<Bitmap>()
                val fallbackColor = if (isPng) Color.TRANSPARENT else Color.WHITE
                for ((minVal, maxVal) in thresholds) {
                    val layerPixels = IntArray(origW * origH)
                    for (i in origPixels.indices) {
                        val d = fullDepth[i]
                        layerPixels[i] = if (d >= minVal && d < maxVal) origPixels[i] else fallbackColor
                    }
                    val layerBitmap = Bitmap.createBitmap(origW, origH, Bitmap.Config.ARGB_8888)
                    layerBitmap.setPixels(layerPixels, 0, origW, 0, 0, origW, origH)
                    layers.add(layerBitmap)
                }
                layers
            }
        }
    }
}
