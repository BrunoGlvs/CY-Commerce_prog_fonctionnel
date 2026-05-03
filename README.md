# CY-Commerce : Système de vente distribué et vérification formelle

Ce projet implémente un système de gestion de commandes e-commerce en utilisant le modèle d'acteurs pour la partie applicative et les réseaux de Pétri pour la validation formelle. L'objectif est de démontrer comment la théorie mathématique permet d'anticiper des erreurs de conception dans un système distribué.

## 1. Architecture du Système

Le projet est divisé en deux modules distincts mais complémentaires, tous deux développés en Scala.

### Implémentation Akka (Traitement des flux)
Nous utilisons Akka Typed pour gérer la logique métier. Le modèle d'acteurs est particulièrement adapté ici car il garantit l'encapsulation de l'état et l'absence de verrous partagés.

*   **ClientActor** : Agit comme l'interface utilisateur, initiant les commandes de manière asynchrone.
*   **MagasinActor** : Gère l'état du stock. Puisqu'un acteur traite ses messages de manière séquentielle dans sa "mailbox", les problèmes de concurrence (Race Conditions) sont résolus par nature : il est impossible que deux transactions décrémentent le stock simultanément de façon erronée.
*   **LivraisonActor** : Un service tiers simulé qui ne reçoit d'ordres que si le MagasinActor a validé la disponibilité des produits.

### Moteur de Pétri (Vérification formelle)
Contrairement à l'utilisation de logiciels tiers (type TINA ou CPN Tools), nous avons développé un moteur d'exécution de réseaux de Pétri intégré au projet. Il permet d'explorer l'espace d'états complet du système pour vérifier des propriétés logiques.

## 2. Analyse Théorique et Propriétés LTL

Le dossier `com/cycommerce/petrinet` contient la logique de vérification. Nous modélisons le processus d'achat par des places (Stock, Demande, Validation) et des transitions.

### Invariant de Sûreté (Safety)
L'algorithme vérifie par induction sur le graphe d'accessibilité que la propriété **Stock >= 0** est un invariant. Peu importe l'ordre ou le nombre de demandes, le système ne peut jamais tomber dans un état où le stock est négatif.

### Propriété de Vivacité et Deadlock
Nous avons testé la propriété de vivacité suivante en Logique Temporelle Linéaire (LTL) :
`[] (Demande_Passée => <> Commande_Validée)`
*(Il est toujours vrai que si une demande est passée, elle sera éventuellement validée).*

**Observation critique :** Le moteur de vérification identifie un **Deadlock** (blocage) lorsque le stock atteint zéro. Dans un modèle mathématique pur, la transition de validation ne peut plus être franchie, laissant le jeton "Demande" bloqué indéfiniment. 

**Application pratique :** Cette découverte nous a conduits à implémenter une branche alternative dans notre code Akka : l'envoi d'un message `StockInsuffisant`. Cela permet de "consommer" la demande même en cas d'échec, évitant ainsi que l'acteur client ne reste en attente indéfinie.

## 3. Scénario de Test (Stress Test)

Le fichier `Main.scala` exécute la simulation suivante :
1.  Initialisation d'un stock de 2 unités.
2.  Alice envoie une commande pour 1 unité.
3.  Bob, au même instant, envoie une commande pour 2 unités.
4.  Le système traite Alice (Stock restant : 1), puis refuse Bob car le stock est insuffisant pour sa demande spécifique.
5.  Le système reste stable et prêt à recevoir de nouvelles commandes.

## 4. Instructions d'Exécution

### Prérequis
*   Java JDK 11 ou supérieur.
*   sbt (Scala Build Tool).

### Lancer la vérification de Pétri
Cette commande exécute l'analyseur formel qui prouve les invariants et détecte les blocages théoriques.
```bash
sbt "runMain com.cycommerce.petrinet.EcommerceModel"
