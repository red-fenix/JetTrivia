package com.example.jettrivia.component

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Light
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jettrivia.model.QuestionItem
import com.example.jettrivia.screens.QuestionsViewModel

@Composable
fun Questions(viewModel: QuestionsViewModel) {
//    val questions = viewModel.data.value.data?.toMutableList()
    val questions = viewModel.data.value.data

    val questionIndex = remember {
        mutableIntStateOf(0)
    }

    if (viewModel.data.value.loading == true) {
        CircularProgressIndicator()
    } else {
        val question = try {
            questions?.get(questionIndex.intValue)
        } catch (ex: Exception) {
            null
        }

        if (questions != null) {
//            QuestionDisplay(question = question!!, questionIndex = questionIndex, viewModel = viewModel, onNextClicked = { questionIndex.intValue++ })
            QuestionDisplay(question = question!!, questionIndex = questionIndex, viewModel = viewModel) { questionIndex.intValue++ }
        }
    }

    Log.d("SIZE", "Questions: ${questions?.size}")
}

@Composable
fun QuestionDisplay(question: QuestionItem,
                    questionIndex: MutableState<Int>,
                    viewModel: QuestionsViewModel,
                    onNextClicked: (Int) -> Unit = {}) {

    val choiceState = remember(question) {
        question.choices.toMutableStateList()
    }

    val answerState = remember(question) {
        mutableIntStateOf(-1)
    }

    val correctAnswerState = remember(question) {
        mutableStateOf<Boolean>(false)
    }

    val updateAnswer: (Int) -> Unit = remember(question) {
        {
            answerState.intValue = it
            correctAnswerState.value = choiceState[it] == question.answer
        }
    }

    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

    Surface (modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(),
            color = MaterialTheme.colorScheme.primaryContainer){

        Column(modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start) {

            if (questionIndex.value >= 3) {
                ShowProgress(score = questionIndex.value)
            }

            QuestionTracker(counter = questionIndex.value+1, outOff = viewModel.getTotalQuestionCount()+1)
//            HorizontalDivider()
            DrawDottedLine(pathEffect)
            Column {
                Text(text = question.question,
                    modifier = Modifier
                        .padding(6.dp)
                        .align(alignment = Alignment.Start)
                        .fillMaxHeight(0.3f),
                    fontSize = 17.sp,
                    fontWeight = Bold,
                    lineHeight = 22.sp
                )

                // Choices
                choiceState.forEachIndexed { index, anwerText ->
                    Row(modifier = Modifier
                        .padding(3.dp)
                        .fillMaxWidth()
                        .height(45.dp)
                        .border(
                            width = 4.dp, brush = Brush.linearGradient(
                                colors = listOf(Color.Blue, Color.Blue)
                            ),
                            shape = RoundedCornerShape(15.dp)
                        )
                        .clip(
                            RoundedCornerShape(
                                topStartPercent = 50,
                                topEndPercent = 50,
                                bottomEndPercent = 50,
                                bottomStartPercent = 50
                            )
                        )
                        .background(color = Color.Transparent),
                            verticalAlignment = Alignment.CenterVertically) {

                        RadioButton(selected = (answerState.intValue == index), onClick = {
                            updateAnswer(index)
                        },
                            modifier = Modifier.padding(start = 16.dp), colors = RadioButtonDefaults.colors(selectedColor = if (correctAnswerState.value && index == answerState.intValue) {
                                Color.Green.copy(alpha = 0.2f)
                            } else {
                                Color.Red.copy(alpha = 0.2f)
                            }
                        )) // end radio button

                        val annotatedString = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = Light, color = if (correctAnswerState.value && index == answerState.intValue) {
                                Color.Green
                            } else if (!correctAnswerState.value && index == answerState.intValue){
                                Color.Red
                            } else {
                                Color.Black
                            })) {
                                append(anwerText)
                            }
                        }
                        Text(text = annotatedString)

                    }
                }

                Button(
                    onClick = {
                        onNextClicked(questionIndex.value)
                    },
                    modifier = Modifier
                        .padding(3.dp)
                        .align(alignment = Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(34.dp),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text(
                        text = "Next",
                        modifier = Modifier.padding(4.dp),
                        color = Color.White,
                        fontSize = 17.sp
                    )
                }
            }

        }
    }
}

@Composable
fun QuestionTracker(counter: Int = 10, outOff: Int = 100) {
    Text(text = buildAnnotatedString {
        withStyle(style = ParagraphStyle(textIndent = TextIndent.None)) {
            withStyle(
                style = SpanStyle(
                    color = Color.Gray,
                    fontWeight = Bold,
                    fontSize = 27.sp
                )
            ) {
                append("Question ${counter}/")

                withStyle(style = SpanStyle(color = Color.Gray, fontWeight = FontWeight.Light, fontSize = 14.sp)) {
                    append("$outOff")
                }
            }
        }
    },
        modifier = Modifier.padding(20.dp))
}


@Composable
fun DrawDottedLine(pathEffect: PathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)) {
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(1.dp), {
        drawLine(color = Color.Gray,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = pathEffect
        )
    })
}

@Preview
@Composable
fun ShowProgress(score: Int = 12) {
    val gradient = Brush.linearGradient(listOf(Color(0xFFF95075), Color(0xFFBE6BE5)))
    val progressFactor = remember(score) {
        mutableFloatStateOf(score*0.005f)
    }

    Row(modifier = Modifier
        .padding(13.dp)
        .fillMaxWidth()
        .height(45.dp)
        .border(
            4.dp, brush = Brush.linearGradient(colors = listOf(Color.Blue, Color.Blue)),
            shape = RoundedCornerShape(34.dp)
        )
        .clip(RoundedCornerShape(50))
        .background(Color.Transparent),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            contentPadding = PaddingValues(1.dp),
            onClick = {},
            modifier = Modifier
                .fillMaxWidth(progressFactor.floatValue)
                .background(brush = gradient),
            enabled = false,
            elevation = null,
            colors = buttonColors(
                contentColor = Color.Transparent,
                disabledContentColor = Color.Transparent
            )
        ) {
            Text(text = "$score", modifier = Modifier
                .clip(shape = RoundedCornerShape(23.dp))
                .fillMaxHeight(0.87f)
                .fillMaxWidth()
                .padding(6.dp),
                color = Color.White,
                textAlign = TextAlign.Start)
        }
    }

}