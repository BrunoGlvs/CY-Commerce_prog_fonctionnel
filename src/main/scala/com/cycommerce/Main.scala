package com.cycommerce

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.cycommerce.actors._
import com.cycommerce.messages.Protocol._

object Main extends App {

  val rootBehavior = Behaviors.setup[String] { context =>
    // 1. Création de l'acteur Livraison
    val livraison = context.spawn(LivraisonActor(), "livraison-actor")

    // 2. Création de l'acteur Magasin (avec un stock initial)
    val stockDepart = Map("PC-Gamer" -> 2, "Souris" -> 10)
    val magasin = context.spawn(MagasinActor(stockDepart, livraison), "magasin-actor")

    // 3. Création de deux acteurs Clients
    val client1 = context.spawn(ClientActor("Client-Alice"), "client-alice")
    val client2 = context.spawn(ClientActor("Client-Bob"), "client-bob")

    // --- SCÉNARIO DE TEST ---
    
    // Alice demande 1 PC
    magasin ! Commander("PC-Gamer", 1, client1)
    
    // Bob demande 2 PCs (Il va y avoir un problème car il n'en restera qu'un seul après Alice !)
    magasin ! Commander("PC-Gamer", 2, client2)

    // Alice veut 5 souris
    magasin ! Commander("Souris", 5, client1)

    Behaviors.empty
  }

  // Démarrage du système d'acteurs Akka
  val system = ActorSystem[String](rootBehavior, "CYCommerceSystem")
}
