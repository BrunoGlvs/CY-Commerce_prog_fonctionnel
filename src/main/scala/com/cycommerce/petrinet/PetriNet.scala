package com.cycommerce.petrinet

// 1. Une "Place" contient des jetons (représente un état passif ou une ressource, ex: "Stock_Magasin")
case class Place(id: String)

// 2. Une "Transition" consomme et produit des jetons (représente une action, ex: "Valider_Commande")
case class Transition(
  id: String,
  inputs: Map[Place, Int], // Les places en entrée et le nombre de jetons requis
  outputs: Map[Place, Int] // Les places en sortie et le nombre de jetons produits
) {
  // Vérifie si la transition peut être franchie
  def isPlayable(marking: Map[Place, Int]): Boolean = {
    inputs.forall { case (place, weight) =>
      marking.getOrElse(place, 0) >= weight
    }
  }

  // Franchit la transition et retourne le nouveau marquage (l'état suivant)
  def fire(marking: Map[Place, Int]): Map[Place, Int] = {
    if (!isPlayable(marking)) throw new Exception(s"La transition $id n'est pas franchissable !")
    
    // On consomme les jetons d'entrée
    val afterConsume = inputs.foldLeft(marking) { case (m, (place, weight)) =>
      m.updated(place, m.getOrElse(place, 0) - weight)
    }
    // On produit les jetons de sortie
    outputs.foldLeft(afterConsume) { case (m, (place, weight)) =>
      m.updated(place, m.getOrElse(place, 0) + weight)
    }
  }
}

// 3. Le Réseau de Pétri global
case class PetriNet(transitions: Set[Transition], initialMarking: Map[Place, Int])
