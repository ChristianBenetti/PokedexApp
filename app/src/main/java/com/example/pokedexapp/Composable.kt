package com.example.pokedexapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pokedexapp.ui.theme.pixelifysans

class Composable {
    companion object{

        //Schermata di caricamento iniziale
        @Composable
        fun FrontLoadingScreen(){
            //Se sta caricando mostra caricamento in corso
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                Column (
                    modifier = Modifier
                        .align(Alignment.Center)
                ){
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(bottom = 15.dp)
                            .align(Alignment.CenterHorizontally)
                    ) // Puoi sostituire con qualsiasi altra animazione di caricamento
                    Text(
                        text = "catturando i pokemon nell'erba alta..",
                        fontFamily = pixelifysans,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

        }


        //Barra di caricamento alla fine della colonna
        @Composable
        fun BottomLoadingCard(){
            Card(
                shape = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 50.dp)
                    .background(Color.Transparent)
            ){
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                    )
                }
            }
        }


        //Card da mostrare in caso di problema di connessione
        @Composable
        fun NetworkErrorCard(string: String){
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 100.dp)
                    .background(Color.Transparent)
            ){
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                ) {
                    Text(
                        text = "Connessione di rete assente",
                        fontFamily = pixelifysans,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = string,
                    fontFamily = pixelifysans,
                    color = colorResource(id = R.color.poke_red),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 10.dp)
                )
            }
        }
    }
}