package com.cycommerce.petrinet

object EcommerceModel extends App {

  // --- 1. DÉFINITION DES PLACES (Ressources et États) ---
  val pClientDemande = Place("Demande_Client")
  val pStockMagasin = Place("Stock_Magasin")
  val pCommandeValidee = Place("Commande_Validee")

  // --- 2. DÉFINITION DES TRANSITIONS (Actions) ---
  // Un client peut prendre 1 élément de stock pour transformer sa demande en commande validée
  val tValiderCommande = Transition(
    id = "T_ValiderCommande",
    inputs = Map(pClientDemande -> 1, pStockMagasin -> 1),
    outputs = Map(pCommandeValidee -> 1)
  )

  // --- 3. CRÉATION DU RÉSEAU ET MARQUAGE INITIAL ---
  // Scénario de crise : 3 clients font une demande, mais il n'y a que 2 produits en stock !
  val initialMarking = Map(
    pClientDemande -> 3, 
    pStockMagasin -> 2,
    pCommandeValidee -> 0
  )

  val petriNet = PetriNet(Set(tValiderCommande), initialMarking)

  // --- 4. EXECUTION DE LA VERIFICATION FORMELLE ---
  println("Simulation et analyse de l'espace d'etats en cours...")
  val states = VerificationEngine.generateStateSpace(petriNet)
  
  println(s"-> Resultat : ${states.size} etats possibles decouverts.\n")

  // Propriete 1 (Surete / Safety) : Le stock est-il toujours >= 0 ?
  val invariantStock = VerificationEngine.checkSafetyInvariant(states, s => s.getOrElse(pStockMagasin, 0) >= 0)
  println(s"[Surete] Le stock est-il toujours positif ou nul ? : $invariantStock")

  // Propriete 2 : (Liveness Limitee/Deadlock) Y a-t-il des blocages ?
  val deadlocks = VerificationEngine.findDeadlocks(petriNet, states)
  println(s"[Vivacite] Nombre d'etats terminaux (Deadlocks) : ${deadlocks.size}")
  
  deadlocks.foreach { state =>
    println(s"  - Etat bloquant trouve : $state")
    val nbClientsBloques = state.getOrElse(pClientDemande, 0)
    if (nbClientsBloques > 0) {
      println(s"  [!] DANGER LTL : La propriete 'Toute demande finit par etre validee' est FAUSSE.")
      val message = if (nbClientsBloques == 1) "client reste bloque" else "clients restent bloques"
      println(s"  Ici, $nbClientsBloques $message (Famine) car le stock est vide !")
    }
  }
}
