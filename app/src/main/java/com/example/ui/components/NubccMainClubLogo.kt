// © 2026 App Owner. All rights reserved.
package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R

/**
 * Displays the Logo Studio brand emblem asset.
 */
@Composable
fun NubccMainClubLogo(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(R.drawable.logo_studio_icon),
        contentDescription = "Logo Studio Icon",
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}
