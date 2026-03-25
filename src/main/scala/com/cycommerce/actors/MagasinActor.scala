package com.cycommerce.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.cycommerce.messages.Protocol._
import akka.actor.typed.ActorRef

object MagasinActor {
  // Le Magasin a un état interne : son stock.
  def apply(stockInitial: Map[String, Int], livraisonActor: ActorRef[LivraisonCommand]): Behavior[MagasinCommand] =
    Behaviors.setup { context =>
      // On passe à un comportement avec état.
      comportementMagasin(stockInitial, livraisonActor)
    }

  private def comportementMagasin(stockActuel: Map[String, Int], livraisonActor: ActorRef[LivraisonCommand]): Behavior[MagasinCommand] =
    Behaviors.receive { (context, message) =>
      message match {
        case Commander(produitId, quantite, client) =>
          val currentStock = stockActuel.getOrElse(produitId, 0)
          
          if (currentStock >= quantite) {
            context.log.info(s"Commande acceptee pour $quantite x $produitId.")
            // On décrémente le stock (Opération critique / invariant)
            val nouveauStock = stockActuel + (produitId -> (currentStock - quantite))
            
            // On informe le client et la livraison
            client ! CommandeValidee(produitId, quantite)
            livraisonActor ! PlanifierLivraison(produitId, quantite, client)
            
            // On change d'état avec le nouveau stock
            comportementMagasin(nouveauStock, livraisonActor)
          } else {
            context.log.warn(s"Stock insuffisant pour $produitId (Demande: $quantite, Disponible: $currentStock).")
            client ! StockInsuffisant(produitId)
            Behaviors.same // L'état du stock ne change pas
          }
      }
    }
}
