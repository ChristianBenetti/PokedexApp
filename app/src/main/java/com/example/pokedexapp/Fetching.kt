package com.example.pokedexapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.MutableState
import com.google.gson.Gson
import me.sargunvohra.lib.pokekotlin.model.Pokemon
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class Fetching {
    //App per fare fetch dei prossimi 20 pokemon a partire da (start + 1)
    companion object {
        suspend fun coFetchPokemon(isLoading: MutableState<Boolean>, pokemonLs: MutableList<Pokemon>, context: Context) {

            //La parte di connessione alle API avviene nel Dispatcher per I/O
            var newPoke = withContext(Dispatchers.IO) {
                Log.d("Network", "Starting network request")

                //Creo una lista temporanea con i prossimi 20 pokemon da caricare
                MutableList(20) { index -> pokeApi.getPokemon(index + pokemonNumber + 1) }
            }


            //La seconda parte che aggiorna la UI nel Main Dispatcher
            withContext(Dispatchers.Main) {

                //Aggiorna la lista di Pokemon, aggiungendo gli ultimi 20 scaricati
                //Ciò chiamerà la ricomposizione della LazyColumn
                pokemonLs.addAll(newPoke)

                val start = pokemonNumber

                isLoading.value = false
                pokemonNumber += 20

                //Aggiorno il numero di Pokemon che sono già stati scaricati
                Log.d("Update", "Stop loading pokemonNumber: ${pokemonNumber}")


                //Di nuovo nel Dispatcher Input Output salvo in locale i nuovi pokemon
                withContext(Dispatchers.IO) {
                    val sharedPokemon = context.getSharedPreferences("POKEMON", Context.MODE_PRIVATE)
                    val editor = sharedPokemon.edit()

                    //Oggetto Gson, dalla librearia di google
                    val g: Gson = Gson()

                    //Salvo nelle sharedPreferences gli ultimi 20 pokemon fetchati
                    for (ind in 0..19) {
                        //Converte da Pokemon a Json
                        val poke = g.toJson(newPoke[ind])
                        //Scrive la stringa Json associata al numero del Pokemon(chiave)
                        editor.putString((start + ind + 1).toString(), poke)
                    }
                    editor.apply()
                }
            }
        }

        suspend fun coFetchOffline(isLoading: MutableState<Boolean>, pokemonLs: MutableList<Pokemon>, context: Context) {
            val newPoke = mutableListOf<Pokemon>()

            //Nel dispatcher IO
            withContext(Dispatchers.IO) {

                //Recupera i prossimi 20 pokemon a partire da pokemonNumber (punto di partenza)
                for (ind in pokemonNumber + 1..pokemonNumber + 20) {
                    val sharedPokemon =
                        context.getSharedPreferences("POKEMON", Context.MODE_PRIVATE)
                    val g = Gson()

                    Log.d("Local", "$ind pokemon fetching..")

                    val json: String? = sharedPokemon.getString(ind.toString(), "{}")
                    val poke: Pokemon = g.fromJson(json, Pokemon::class.java)

                    newPoke.add(poke)
                }
            }

            //Parte che aggiorna UI
            withContext(Dispatchers.Main) {
                pokemonNumber += 20
                pokemonLs.addAll(newPoke)
                isLoading.value = false
                Log.d("PokemonNumber update", "Updated to $pokemonNumber")
            }
        }

        //Al pull to refresh rimuove tutti i pokemon salvati in locale
        suspend fun coClearPref(context: Context){
            withContext(Dispatchers.IO) {
                val sharedPokemon = context.getSharedPreferences("POKEMON", Context.MODE_PRIVATE)
                val editor = sharedPokemon.edit()

                editor.clear()
                editor.apply()
            }
        }

        //Controlla se i prossimi 20 pokemon sono presenti i locale
        suspend fun checkDownload(context: Context) : Boolean{
            var download = false
            withContext(Dispatchers.IO) {
                val sharedName = context.getSharedPreferences("POKEMON", Context.MODE_PRIVATE)
                val lastPokemon = pokemonNumber + 20

                Log.d("Check", "Checking for ${lastPokemon} pokemon")

                //Controllo se l'ultimo pokemon del "round" di caricamento è presente nelle preferences
                download = sharedName.getString(lastPokemon.toString(), "not") != "not"
            }
            return download
        }

    }
}