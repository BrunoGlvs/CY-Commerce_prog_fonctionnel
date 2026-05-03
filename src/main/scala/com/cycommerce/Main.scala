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

    // --- VÉRIFICATION FORMELLE (Réseau de Pétri) ---
    // Avant de lancer nos acteurs, on lance l'analyse de notre modèle mathématique
    println("=== 1. ANALYSE FORMELLE DU RÉSEAU DE PETRI ===")
    com.cycommerce.petrinet.EcommerceModel.main(Array.empty)
    println("\n=== 2. LANCEMENT DES ACTEURS AKKA ===")

    // --- SCÉNARIO DE TEST AKKA (Basé sur l'analyse de Petri) ---
    
    // Cas 1: Scénario Nominal (Tout se passe bien)
    // Alice demande 1 PC-Gamer. Stock dispo : 2. (Validé + Livré)
    magasin ! Commander("PC-Gamer", 1, client1)
    
    // Cas 2: Famine / Concurrence (Comme détecté par le Réseau de Pétri)
    // Bob demande 2 PC-Gamer. Mais il n'en reste qu'un seul à cause d'Alice. (Refusé)
    magasin ! Commander("PC-Gamer", 2, client2)

    // Cas 3: Nouveau client, nouvelle commande
    val client3 = context.spawn(ClientActor("Client-Charlie"), "client-charlie")
    
    // Charlie veut prendre tout le stock de souris (Stock: 10)
    magasin ! Commander("Souris", 10, client3)
    
    // Cas 4: Stock à 0 (Vérification de Sûreté - Invariant)
    // Alice réessaie d'acheter des souris alors que Charlie a tout vidé.
    magasin ! Commander("Souris", 5, client1)

    Behaviors.empty
  }

  // Démarrage du système d'acteurs Akka
  val system = ActorSystem[String](rootBehavior, "CYCommerceSystem")
}
