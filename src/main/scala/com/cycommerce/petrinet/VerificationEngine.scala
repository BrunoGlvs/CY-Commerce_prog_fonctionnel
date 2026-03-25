package com.cycommerce.petrinet

object VerificationEngine {

  /**
   * Génère tout l'espace d'états possible (Graphe de couverture/accessibilité).
   * C'est la base pour faire de la vérification formelle (Model Checking).
   */
  def generateStateSpace(net: PetriNet): Set[Map[Place, Int]] = {
    var visited = Set[Map[Place, Int]]()
    var stack = List(net.initialMarking)

    while (stack.nonEmpty) {
      val currentMarking = stack.head
      stack = stack.tail

      if (!visited.contains(currentMarking)) {
        visited += currentMarking
        // On explore toutes les transitions possibles depuis l'état courant
        val nextMarkings = net.transitions
          .filter(_.isPlayable(currentMarking))
          .map(_.fire(currentMarking))
        
        stack ++= nextMarkings
      }
    }
    visited
  }

  /**
   * Vérifie un "Invariant de sûreté" (Safety Property).
   * Ex: Le stock ne doit jamais être négatif.
   */
  def checkSafetyInvariant(stateSpace: Set[Map[Place, Int]], predicate: Map[Place, Int] => Boolean): Boolean = {
    stateSpace.forall(predicate)
  }

  /**
   * Vérifie l'absence de Deadlocks.
   * Retourne la liste des états depuis lesquels aucune transition ne peut être tirée.
   */
  def findDeadlocks(net: PetriNet, stateSpace: Set[Map[Place, Int]]): Set[Map[Place, Int]] = {
    stateSpace.filter(state => net.transitions.forall(t => !t.isPlayable(state)))
  }
}
