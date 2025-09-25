package com.example.multi.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.multi.MedallionScreen
import com.example.multi.MedallionSegment
import com.example.multi.R
import com.example.multi.SegmentScreen

@Composable
fun MultiApp(startDestination: AppDestination) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination.route
    ) {
        onboardingDestination(navController)
        lockDestination(navController)
        homeDestination(navController)
        segmentDestination(navController, AppDestination.Notes) { onNavigateUp ->
            PlaceholderSegmentScreen(destination = AppDestination.Notes, onNavigateUp = onNavigateUp)
        }
        segmentDestination(navController, AppDestination.Goals) { onNavigateUp ->
            PlaceholderSegmentScreen(destination = AppDestination.Goals, onNavigateUp = onNavigateUp)
        }
        segmentDestination(navController, AppDestination.Events) { onNavigateUp ->
            PlaceholderSegmentScreen(destination = AppDestination.Events, onNavigateUp = onNavigateUp)
        }
        segmentDestination(navController, AppDestination.Calendar) { onNavigateUp ->
            PlaceholderSegmentScreen(destination = AppDestination.Calendar, onNavigateUp = onNavigateUp)
        }
    }
}

private fun NavGraphBuilder.onboardingDestination(navController: NavHostController) {
    composable(AppDestination.Onboarding.route) {
        val context = LocalContext.current
        val appContext = context.applicationContext
        val resolver = remember(appContext) {
            AppStartDestinationResolver(appContext)
        }
        OnboardingScreen(
            onContinue = {
                resolver.markOnboardingComplete()
                navController.navigate(AppDestination.Home.route) {
                    popUpTo(AppDestination.Onboarding.route) { inclusive = true }
                }
            }
        )
    }
}

private fun NavGraphBuilder.lockDestination(navController: NavHostController) {
    composable(AppDestination.Lock.route) {
        val context = LocalContext.current
        val appContext = context.applicationContext
        val resolver = remember(appContext) {
            AppStartDestinationResolver(appContext)
        }
        LockScreen(
            onUnlock = {
                resolver.disableLock()
                navController.navigate(AppDestination.Home.route) {
                    popUpTo(AppDestination.Lock.route) { inclusive = true }
                }
            }
        )
    }
}

private fun NavGraphBuilder.homeDestination(navController: NavHostController) {
    composable(AppDestination.Home.route) {
        HomeScreen(onSegmentSelected = { segment ->
            val destination = AppDestination.fromSegment(segment)
            navController.navigate(destination.route) {
                launchSingleTop = true
                restoreState = true
            }
        })
    }
}

private fun NavGraphBuilder.segmentDestination(
    navController: NavHostController,
    destination: AppDestination,
    content: @Composable (onNavigateUp: () -> Unit) -> Unit
) {
    composable(destination.route) {
        content {
            if (!navController.popBackStack()) {
                navController.navigate(AppDestination.Home.route) {
                    popUpTo(AppDestination.Home.route) { inclusive = false }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }
}

@Composable
private fun OnboardingScreen(onContinue: () -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.onboarding_title),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(id = R.string.onboarding_body),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onContinue) {
                Text(text = stringResource(id = R.string.action_continue))
            }
        }
    }
}

@Composable
private fun LockScreen(onUnlock: () -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.lock_title),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(id = R.string.lock_body),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onUnlock) {
                Text(text = stringResource(id = R.string.action_unlock))
            }
        }
    }
}

@Composable
private fun HomeScreen(onSegmentSelected: (MedallionSegment) -> Unit) {
    MedallionScreen(onNavigateToSegment = onSegmentSelected)
}

@Composable
private fun PlaceholderSegmentScreen(
    destination: AppDestination,
    onNavigateUp: () -> Unit
) {
    val title = destination.labelRes?.let { stringResource(id = it) } ?: destination.route
    SegmentScreen(
        title = title,
        onBack = onNavigateUp,
        onClose = onNavigateUp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.segment_placeholder, title),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onNavigateUp) {
                Text(text = stringResource(id = R.string.action_go_back))
            }
        }
    }
}
