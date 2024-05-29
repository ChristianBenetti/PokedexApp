package com.example.pokedexapp.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.pokedexapp.R

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val ColorMap = mapOf<String,Int>(
    "fighting" to R.color.fighting,
    "psychic" to R.color.psychic,
    "poison" to R.color.poison,
    "dragon" to R.color.dragon,
    "ghost" to R.color.ghost,
    "dark" to R.color.dark,
    "ground" to R.color.ground,
    "fire" to R.color.fire,
    "fairy" to R.color.fairy,
    "water" to R.color.water,
    "flying" to R.color.flying,
    "normal" to R.color.normal,
    "rock" to R.color.rock,
    "electric" to R.color.electric,
    "bug" to R.color.bug,
    "grass" to R.color.grass,
    "ice" to R.color.ice,
    "steel" to R.color.steel
)

//A partire da due tipi Pokemon, ritorna i colori mappati
fun getColorType(type: String, type2: String = type): Pair<Int, Int>{
    return Pair(ColorMap[type]!!, ColorMap[type2]!!)
}
