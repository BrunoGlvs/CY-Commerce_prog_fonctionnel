package com.cycommerce.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.cycommerce.messages.Protocol._

object ClientActor {
  def apply(clientId: String): Behavior[ClientCommand] = Behaviors.receive { (context, message) =>
    message match {
      case CommandeValidee(produitId, quantite) =>
        context.log.info(s"[$clientId] Super ! Ma commande de $quantite $produitId est validee.")
        Behaviors.same

      case StockInsuffisant(produitId) =>
        context.log.warn(s"[$clientId] Mince... Il n'y a plus assez de $produitId en stock.")
        Behaviors.same

      case LivraisonPlanifiee(produitId, date) =>
        context.log.info(s"[$clientId] Je serai livre de mon $produitId le $date.")
        Behaviors.same
    }
  }
}
