package nl.expeler.einkteletext.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import nl.expeler.einkteletext.model.FastTextLink
import nl.expeler.einkteletext.model.TeletextPage
import nl.expeler.einkteletext.ui.TeletextLine
import nl.expeler.einkteletext.ui.parseTeletextContent
import nl.expeler.einkteletext.ui.theme.*
import nl.expeler.einkteletext.viewmodel.TeletextUiState
import nl.expeler.einkteletext.viewmodel.TeletextViewModel

@Composable
fun TeletextScreen(vm: TeletextViewModel = viewModel()) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val visited by vm.visited.collectAsStateWithLifecycle()

    BackHandler(enabled = vm.canGoBack) {
        vm.goBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EinkPaper)
            .systemBarsPadding()
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (val state = uiState) {
                is TeletextUiState.Loading -> LoadingView(showLogo = state.isInitial)
                is TeletextUiState.Error -> ErrorView(state.message)
                is TeletextUiState.Success -> TeletextContent(
                    page = state.page,
                    visited = visited,
                    onPageClick = { vm.loadPage(it) },
                    onPrevSubPage = { vm.navigatePrevSubPage() },
                    onNextSubPage = { vm.navigateNextSubPage() },
                )
            }
        }

        HorizontalDivider(color = EinkInk, thickness = 1.dp)

        val successState = uiState as? TeletextUiState.Success
        val fastTextLinks = successState?.page?.fastTextLinks
        if (!fastTextLinks.isNullOrEmpty()) {
            FastTextBar(
                links = fastTextLinks,
                prevSubPage = successState?.page?.prevSubPage.orEmpty(),
                nextSubPage = successState?.page?.nextSubPage.orEmpty(),
                onLinkClick = { vm.loadPage(it) },
                onPrevSubPage = { vm.navigatePrevSubPage() },
                onNextSubPage = { vm.navigateNextSubPage() },
            )
        }
    }
}

@Composable
private fun TeletextContent(
    page: TeletextPage,
    visited: Set<String>,
    onPageClick: (String) -> Unit,
    onPrevSubPage: () -> Unit,
    onNextSubPage: () -> Unit,
) {
    val lines = remember(page.content) { parseTeletextContent(page.content) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val horizontalPadding = 28.dp
        val availableDp = maxWidth - horizontalPadding
        val fontScale = LocalDensity.current.fontScale
        val fontSize = (availableDp.value / 40f / 0.60f / fontScale)
            .coerceIn(8f, 16f).sp

        var dragX = 0f
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(page.prevSubPage, page.nextSubPage) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragX = 0f },
                        onHorizontalDrag = { _, delta -> dragX += delta },
                        onDragEnd = {
                            if (dragX < -80f && page.nextSubPage.isNotBlank()) onNextSubPage()
                            else if (dragX > 80f && page.prevSubPage.isNotBlank()) onPrevSubPage()
                        }
                    )
                }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            lines.forEach { line ->
                TeletextRow(
                    line = line,
                    fontSize = fontSize,
                    isVisited = line.page != null && line.page in visited,
                    onPageClick = onPageClick,
                )
            }
            if (page.prevSubPage.isNotBlank() || page.nextSubPage.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    androidx.compose.material3.Text(
                        text = if (page.prevSubPage.isNotBlank()) "‹" else "",
                        fontFamily = FontFamily.Monospace,
                        fontSize = fontSize,
                        color = EinkInk,
                        modifier = Modifier.clickable(enabled = page.prevSubPage.isNotBlank()) { onPrevSubPage() },
                    )
                    androidx.compose.material3.Text(
                        text = if (page.nextSubPage.isNotBlank()) "›" else "",
                        fontFamily = FontFamily.Monospace,
                        fontSize = fontSize,
                        color = EinkInk,
                        modifier = Modifier.clickable(enabled = page.nextSubPage.isNotBlank()) { onNextSubPage() },
                    )
                }
            }
        }
    }
}

@Composable
private fun TeletextRow(
    line: TeletextLine,
    fontSize: TextUnit,
    isVisited: Boolean = false,
    onPageClick: (String) -> Unit,
) {
    val rowStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = fontSize,
        lineHeight = fontSize * 1.3f,
        color = if (isVisited) EinkMidGray else EinkInk,
        fontStyle = if (isVisited) FontStyle.Italic else FontStyle.Normal,
        fontWeight = if (isVisited) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Start,
    )

    if (line.isTitle) {
        androidx.compose.material3.Text(
            text = line.text,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = fontSize * 1.1f,
                lineHeight = fontSize * 1.4f,
                fontWeight = FontWeight.Bold,
                color = EinkInk,
                textAlign = TextAlign.Start,
            ),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
        )
        return
    }

    if (line.page != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPageClick(line.page) }
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material3.Text(
                text = line.text,
                style = rowStyle,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                modifier = Modifier.weight(1f),
            )
            androidx.compose.material3.Text(
                text = " ›",
                color = EinkMidGray,
                fontSize = fontSize,
                fontFamily = FontFamily.Monospace,
            )
        }
        HorizontalDivider(color = EinkLightGray, thickness = 0.5.dp)
    } else {
        androidx.compose.material3.Text(
            text = line.text,
            style = rowStyle,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
        )
    }
}

@Composable
private fun FastTextBar(
    links: List<FastTextLink>,
    prevSubPage: String,
    nextSubPage: String,
    onLinkClick: (String) -> Unit,
    onPrevSubPage: () -> Unit,
    onNextSubPage: () -> Unit,
) {
    HorizontalDivider(color = EinkLightGray, thickness = 0.5.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(EinkPaper)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        links.filter { it.title.lowercase() != "weer" }.take(4).forEachIndexed { index, link ->
            if (index > 0) {
                androidx.compose.material3.Text(
                    text = "·",
                    color = EinkLightGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                )
            }
            androidx.compose.material3.Text(
                text = link.title.lowercase(),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = EinkMidGray,
                modifier = Modifier.clickable { onLinkClick(link.page) },
            )
        }
    }
}

@Composable
private fun LoadingView(showLogo: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EinkPaper),
        contentAlignment = Alignment.Center,
    ) {
        if (showLogo) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.material3.Text(
                    text = "█▀█ █▀█ █▀",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = EinkInk,
                    letterSpacing = 2.sp,
                )
                androidx.compose.material3.Text(
                    text = "█ █ █ █ ▄█",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = EinkInk,
                    letterSpacing = 2.sp,
                )
                Spacer(modifier = Modifier.height(20.dp))
                androidx.compose.material3.Text(
                    text = "TELETEKST",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = EinkInk,
                    letterSpacing = 4.sp,
                )
                Spacer(modifier = Modifier.height(32.dp))
                androidx.compose.material3.Text(
                    text = "· · ·",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    color = EinkMidGray,
                )
            }
        } else {
            androidx.compose.material3.Text(
                text = "· · ·",
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                color = EinkMidGray,
            )
        }
    }
}

@Composable
private fun ErrorView(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.material3.Text(
                text = "PAGINA NIET BESCHIKBAAR",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = EinkInk,
            )
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.material3.Text(
                text = message,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = EinkMidGray,
            )
        }
    }
}
