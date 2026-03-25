package com.cycommerce.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.cycommerce.messages.Protocol._

object LivraisonActor {
  def apply(): Behavior[LivraisonCommand] = Behaviors.receive { (context, message) =>
    message match {
      case PlanifierLivraison(produitId, quantite, client) =>
        context.log.info(s"Processus d'expedition demarre pour $quantite x $produitId.")
        // Simulation de planification
        val dateLivraison = "Demain matin"
        client ! LivraisonPlanifiee(produitId, dateLivraison)
        Behaviors.same
    }
  }
}
