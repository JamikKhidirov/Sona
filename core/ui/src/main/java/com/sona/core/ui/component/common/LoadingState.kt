package com.sona.core.ui.component.common

import android.R.attr.indeterminateDuration
import android.view.LayoutInflater
import android.widget.ProgressBar
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.sona.core.ui.R

@Composable
@Preview(showBackground = true)
fun LoadingState(
    modifier: Modifier = Modifier,
    label: String = "В поиске...",
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AndroidView(
            modifier = modifier.size(34.dp),
            factory = { context ->
                LayoutInflater
                    .from(context)
                    .inflate(R.layout.view_fast_progress, null, false) as ProgressBar
            }
        )

        Spacer(Modifier.height(14.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
        )
    }
}