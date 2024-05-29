package com.example.pokedexapp


import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.pokedexapp.ui.theme.PokedexAppTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext


import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList


import me.sargunvohra.lib.pokekotlin.client.PokeApi
import me.sargunvohra.lib.pokekotlin.client.PokeApiClient
import me.sargunvohra.lib.pokekotlin.model.Pokemon

import coil.compose.AsyncImage
import kotlinx.coroutines.*
import com.example.pokedexapp.Fetching.Companion.checkDownload
import com.example.pokedexapp.Network.Companion.isConnected
import com.example.pokedexapp.Fetching.Companion.coFetchOffline
import com.example.pokedexapp.Fetching.Companion.coClearPref
import com.example.pokedexapp.Fetching.Companion.coFetchPokemon

import com.example.pokedexapp.Composable.Companion.NetworkErrorCard
import com.example.pokedexapp.Composable.Companion.BottomLoadingCard
import com.example.pokedexapp.Composable.Companion.FrontLoadingScreen

import com.example.pokedexapp.ui.theme.getColorType
import com.example.pokedexapp.ui.theme.pixelifysans

import me.sargunvohra.lib.pokekotlin.model.PokemonMove
import me.sargunvohra.lib.pokekotlin.model.PokemonType


val pokeApi : PokeApi = PokeApiClient()
//Numero di pokemon attualmente caricati sulla UI
var pokemonNumber : Int = 0

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokedexAppTheme {
                PokePreview()
            }
        }
    }
}


//Lazy column contenente la lista di Pokemon contenuti in pokemonLs
@Composable
fun Scrolling(isLoading:  MutableState<Boolean>, pokemonLs: SnapshotStateList<Pokemon>, isConnected: MutableState<Boolean>){
    val context : Context = LocalContext.current

    //Per gestire lo stato di scorriemento della LazyColumn
    val listState = rememberLazyListState()

    //Se si sta caricando per la prima volta, FrontLoadingScreen
    if (isLoading.value && pokemonNumber==0) {FrontLoadingScreen()}


    LazyColumn(
        //Per tenere traccia dello stato della lista
        state = rememberLazyListState(),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        modifier = Modifier
            .padding(top = 110.dp)
    )
    {
        //Aggiunge i componenti della lista pokemonLs al composable
        items(pokemonLs) { pokemon ->
            PokemonCard(pokemon)
        }
        item {
            //Quando si raggiunge il fondo della lista e non sono finiti i pokemon
            LaunchedEffect(!listState.canScrollForward && pokemonNumber < 1020) {

                //Controlla se i prossimi 20 pokemon da caricare sono gia presenti in locale
                if(checkDownload(context)){
                    isLoading.value = true
                    //Se si li recupera dalla memoria
                    coFetchOffline(isLoading, pokemonLs, context)
                }
                else{
                    //Altrimenti controlla se c è connessione internet
                    isConnected(context, isConnected= isConnected)
                    if(isConnected.value) {
                        isLoading.value = true
                        //Chiama il fetch online dei successivi 20 pokemon
                        coFetchPokemon(isLoading,pokemonLs,context)
                    }
                    else {
                        //Senno finisce il caricamento
                        isLoading.value = false
                    }
                }
            }
        }
        item {
            //Mostra la barra di caricamento in fondo alla colonna
            if (isLoading.value && pokemonNumber != 0) {
                BottomLoadingCard()
            }
        }
        item {
            //Mostra la card di errore di rete
            if(!isConnected.value && pokemonNumber != 0){
                NetworkErrorCard(string = "Scorri di nuovo per riprovare")
            }
            else
            //Mostra la card di errore di rete al centro della schermata iniziale
                if(!isConnected.value && pokemonNumber == 0){
                    Box(
                        contentAlignment = Alignment.TopEnd,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                        ) {
                            NetworkErrorCard(string = "Tira giù per riprovare")
                        }
                    }
                }
        }
    }
}


//Utilizzando rememberPullRefreshState e PullRefreshIndicator (sperimentali)
//Implementa il pull to refresh
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PullToRefresh(modifier: Modifier = Modifier)
{
    //Stato per gestire schermate di loading
    val isLoading = remember { mutableStateOf(true)}
    //Stato per verificare collegamento alla rete
    val isConnected = remember { mutableStateOf(true)}
    //Stato per gestire il refreshing
    var isRefreshing by remember { mutableStateOf(false) }

    //Stato Lista di pokemon presenti sulla UI
    val pokemonLs = remember { mutableStateListOf<Pokemon>()}

    //Scope della coroutine
    val coroutineScope = rememberCoroutineScope()

    //Contesto attuale
    val context = LocalContext.current

    //Struttura per implementare pull to refresh
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        refreshingOffset = 0.dp,
        refreshThreshold = 150.dp,
        onRefresh = {
            //Controllo connessione
            isConnected(context, isConnected)
            if(isConnected.value) {
                //Si sta refreshando
                isRefreshing = true
                coroutineScope.launch {
                    //Setto a 0 il numero di pokemon, pulisco pokemonLs e le sharedPreferences
                    pokemonNumber = 0
                    pokemonLs.clear()
                    isLoading.value = true

                    coClearPref(context)
                    //Una volta finito non si sta piu refreshando
                    isRefreshing = false
                }
            }
        }
    )
    //Box che contiene il componente con la lista di pokemon e l indicatore di refresh
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
        .pullRefresh(pullRefreshState)
    )
    {
    Scrolling(isLoading= isLoading, pokemonLs = pokemonLs, isConnected= isConnected)

    PullRefreshIndicator(
        refreshing = isRefreshing,
        state = pullRefreshState,
        backgroundColor = colorResource(id = R.color.poke_red),
        contentColor = colorResource(id = R.color.poke_grey),
        modifier = Modifier
            .align(Alignment.TopCenter)
    )
    }


}




//Composable che rappresenta una singola Card con un Pokemon
@Composable
fun PokemonCard(pokemon: Pokemon) {

    //Stato per cambiare immagine al click sulla stess
    var shiny by remember { mutableStateOf(false)}
    //Stato che gestisce espansione Card
    var expanded by remember { mutableStateOf(false) }

    //Lista contenente tutti i tipi Pokemon
    val types: List<PokemonType>  = pokemon.types

    //Crea un Pair di colori che contiene i colori corrispondenti ai tipi del pokemon
    val colors: Pair<Int,Int> = if(types.size==2){
        getColorType(types[0].type.name, types[1].type.name)
    }
    else {
        //Se c è un solo colore la scheda è uniforme
        getColorType(types[0].type.name, types[0].type.name)
    }

    //Stringa che concatena i tipi pokemon divisi da ','
    val typeString = types.joinToString(separator = ", ") { type -> type.type.name }

    Card(
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 15.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = Color.Black
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .height(if (expanded) 500.dp else 150.dp)
            .animateContentSize(
                animationSpec = spring()
            )
            .background(Color.Transparent)
            .clickable {
                expanded = !expanded
            }
    ) {
        //Riga contenente tutti gli elementi della Card
        Row(
            horizontalArrangement = Arrangement.spacedBy(0.dp),

                modifier = Modifier
                    .fillMaxWidth()
                    //.fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                colorResource(id = colors.first),
                                colorResource(id = colors.second)
                            )
                        )
                    )
            ) {
                //Colonna con Id pokemon
                Column {
                    Text(
                        text = '#' + pokemon.id.toString(),
                        fontWeight = FontWeight.Bold,
                        fontFamily = pixelifysans,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Right,
                        modifier = Modifier
                            .align(Alignment.Start)
                    )
                }
                //Box con immagine
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(horizontal = 3.dp)
                        .clickable { shiny = !shiny }
                ) {
                    when (shiny) {
                        false -> AsyncImage(
                            model = pokemon.sprites.frontDefault,
                            contentDescription = pokemon.sprites.frontDefault.toString(),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .matchParentSize()
                        )

                        true -> AsyncImage(
                            model = pokemon.sprites.frontShiny,
                            contentDescription = pokemon.sprites.frontShiny.toString(),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .matchParentSize()
                        )
                    }
                }
                //Colonna con nome e tipi del Pokemon
                Column {
                    Text(
                        text = pokemon.name,
                        fontWeight = FontWeight.Bold,
                        fontFamily = pixelifysans,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .padding(top = 20.dp)
                    )
                    Text(
                        text = typeString,
                        fontFamily = pixelifysans,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
        }
            //Se viene espansa la card mostra la lista di mosse
            if (expanded) {
                MovesList(pokemon = pokemon, colors = colors)
            }
    }
}


//Lista scorrevole all interno della card con l elenco di mosse del Pokemon
@Composable
fun MovesList(pokemon: Pokemon, colors: Pair<Int,Int>) {
    //Lista con tutte le mosse pokemon
    val moves: List<PokemonMove> = pokemon.moves

    //Lista di stringhe con i nomi delle mosse
    val movesList = moves.map { move -> move.move.name }

    Column (
        verticalArrangement = Arrangement.spacedBy(0.dp),

        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    listOf(
                        colorResource(id = colors.first),
                        colorResource(id = colors.second)
                    )
                )
            )
    ) {
        Text(
            text = "Moveset",
            fontWeight = FontWeight.Bold,
            fontFamily = pixelifysans,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center ,
            modifier = Modifier
                    .align(Alignment.CenterHorizontally)
        )
        //Colonna scorrevole con mosse del pokemon
        LazyColumn (
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
        {
            items(movesList) { move ->
                Text(
                    text = move,
                    fontFamily = pixelifysans,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    showSystemUi = true)
@Composable
fun PokePreview() {
    //Comportamento della topAppBar quando si scrolla la LazyColumn
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    PokedexAppTheme {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            //Barra superiore con logo
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorResource(id = R.color.poke_red),
                        titleContentColor = colorResource(id = R.color.poke_grey),
                    ),
                    title = {
                            Image(
                                painter = painterResource(id = R.drawable.pok_dex_logo),
                                contentDescription = "Pokedex Logo")

                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { innerPadding ->
                PullToRefresh(modifier = Modifier.padding(innerPadding))
        }
    }
}

