package io.github.janmalch.woroboro.ui.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


@Composable
fun SimpleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
) {
    val textColor = LocalTextStyle.current.color.takeOrElse {
        LocalContentColor.current
    }
    Box {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = LocalTextStyle.current.copy(color = textColor),
            cursorBrush = SolidColor(textColor),
            singleLine = singleLine,
            modifier = modifier,
        )
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = textColor.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun TextWithEditSheet(
    value: String,
    placeholder: String,
    buttonText: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    textStyle: TextStyle = LocalTextStyle.current,
) {
    var isSheetVisible by remember {
        mutableStateOf(false)
    }

    Text(text = value,
        style = textStyle,
        modifier = modifier
            .clickable { isSheetVisible = true }
            .padding(contentPadding))

    if (isSheetVisible) {
        TextFieldSheet(
            value = value,
            placeholder = placeholder,
            buttonText = buttonText,
            onValueChange = onValueChange,
            onDismissRequest = { isSheetVisible = false }
        )
    }
}


@Composable
fun TextFieldSheet(
    value: String,
    placeholder: String,
    buttonText: String,
    onValueChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    var textValue by remember {
        mutableStateOf(value)
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {

            SimpleTextField(
                value = textValue,
                onValueChange = { textValue = it },
                placeholder = placeholder,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )

            TextButton(
                modifier = Modifier.align(Alignment.End),
                enabled = textValue.isNotBlank(),
                onClick = {
                    focusRequester.freeFocus()
                    onValueChange(textValue)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onDismissRequest()
                        }
                    }
                }
            ) {
                Text(text = buttonText)
            }

            LaunchedEffect(Unit) {
                focusManager.clearFocus(force = true)
                focusRequester.requestFocus()
            }
        }
    }
}
