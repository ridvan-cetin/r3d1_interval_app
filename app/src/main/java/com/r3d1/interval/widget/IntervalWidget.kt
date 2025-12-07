package com.r3d1.interval.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.r3d1.interval.data.model.Profile
import com.r3d1.interval.service.TimerService
import com.r3d1.interval.ui.main.MainActivity

class IntervalWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            IntervalWidgetContent()
        }
    }
}

@Composable
fun IntervalWidgetContent() {
    val context = LocalContext.current

    GlanceTheme {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFF1E1E1E))
                .cornerRadius(16.dp)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Vertical.Top,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Text(
                    text = "R3D1 Interval",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Quick start buttons
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                ) {
                    QuickStartButton(
                        name = "Meeting",
                        color = Color(0xFF2196F3),
                        profileId = 1L
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    QuickStartButton(
                        name = "Coding",
                        color = Color(0xFF4CAF50),
                        profileId = 2L
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                ) {
                    QuickStartButton(
                        name = "Chat",
                        color = Color(0xFFFF9800),
                        profileId = 3L
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    QuickStartButton(
                        name = "Quick",
                        color = Color(0xFFE91E63),
                        profileId = 4L
                    )
                }
            }
        }
    }
}

@Composable
fun QuickStartButton(
    name: String,
    color: Color,
    profileId: Long
) {
    Box(
        modifier = GlanceModifier
            .width(70.dp)
            .height(36.dp)
            .background(color.copy(alpha = 0.3f))
            .cornerRadius(8.dp)
            .clickable(
                actionRunCallback<StartTimerAction>(
                    actionParametersOf(profileIdKey to profileId)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = TextStyle(
                color = ColorProvider(color),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

val profileIdKey = ActionParameters.Key<Long>("profile_id")

class StartTimerAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val profileId = parameters[profileIdKey] ?: return

        // Start MainActivity with the profile to start
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("start_profile_id", profileId)
        }
        context.startActivity(intent)
    }
}

class IntervalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = IntervalWidget()
}
