package com.cycommerce.messages

import akka.actor.typed.ActorRef

// Fichier centralisant tous les messages (Protocoles) échangés entre nos Acteurs.
object Protocol {
  // ----- Messages pour le Magasin -----
  sealed trait MagasinCommand
  case class Commander(produitId: String, quantite: Int, client: ActorRef[ClientCommand]) extends MagasinCommand
  
  // ----- Messages pour le Client -----
  sealed trait ClientCommand
  case class CommandeValidee(produitId: String, quantite: Int) extends ClientCommand
  case class StockInsuffisant(produitId: String) extends ClientCommand
  case class LivraisonPlanifiee(produitId: String, date: String) extends ClientCommand

  // ----- Messages pour la Livraison -----
  sealed trait LivraisonCommand
  case class PlanifierLivraison(produitId: String, quantite: Int, client: ActorRef[ClientCommand]) extends LivraisonCommand
}
