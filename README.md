# Modélisation et Vérification d’une Application E-Commerce avec Akka et Réseaux de Pétri

Bienvenue dans le dépôt du projet ! Ce projet s'inscrit dans le cadre de notre module sur les **Systèmes Distribués et la Programmation Fonctionnelle**.

Il a pour but de modéliser, implémenter et vérifier mathématiquement un scénario critique au sein d'une application distribuée : **La validation d'une commande et la gestion concurrente des stocks dans un e-commerce.**

---

## 📌 1. Contexte & Choix Techniques

Pour répondre aux exigences de robustesse d'un système distribué critique, nous avons conçu l'architecture suivante :
* Conception basée sur le **Modèle d'Acteurs** avec **Scala et Akka Typed**.
* Architecture **totalement asynchrone et distribuée par envois de messages**, garantissant une forte tolérance aux pannes et l'absence de blocages liés à des ressources partagées.
* Création d'un **moteur "Fait-Maison" de Réseaux de Pétri** en Scala (comme exigé par la consigne qui interdisait l'utilisation de logiciels externes).

## ⚙️ 2. L'Application Akka (La Pratique)

Le scénario critique est géré par trois Acteurs principaux :
1. **`ClientActor`** : Représente l'utilisateur. Il envoie des demandes d'achats.
2. **`MagasinActor`** : **C'est la brique critique.** Il gère l'état du stock. Grâce à la nature des Acteurs Akka (qui traitent les messages séquentiellement un par un), **les "Race Conditions" sont impossibles**. Deux clients ne peuvent pas acheter le même dernier produit en même temps.
3. **`LivraisonActor`** : Planifie la livraison si le `MagasinActor` valide la transaction.

Dans le fichier `Main.scala`, nous simulons un **Stress Test** :
* 2 PC-Gamer sont en stock.
* Alice en demande 1.
* Bob arrive au même moment et en demande 2. 
* *Résultat attendu : Alice est servie, Bob reçoit un refus poli, le système ne crash pas et ne bloque pas.*

## 🧮 3. Réseau de Pétri & Logique LTL (La Théorie)

Dans le dossier `com/cy-commerce/petrinet`, nous avons codé un simulateur mathématique. Nous avons défini des *Places* (Stock, Demande_Client, etc.) et des *Transitions* (Valider_Commande).

L'algorithme LTL et Model Checking (`VerificationEngine.scala`) analyse tous les états possibles et prouve 2 choses :

1. **Un Invariant de Sûreté (Safety)** : `Stock_Magasin >= 0`. L'algorithme vérifie tous les chemins et prouve mathématiquement que le stock ne sera jamais négatif.
2. **Une propriété de Vivacité (Liveness / LTL)** : `[] (Demande_Passée => <> Commande_Validee)` (*"Il est Toujours vrai que si une Demande est passée, alors Eventuellement elle sera Validée"*).
   * L'algorithme trouve un **DEADLOCK (Famine / Starvation)** ! 
   * S'il y a 3 demandes pour 2 produits en stock, l'algorithme de Pétri bloque complètement. Le 3ème client tourne dans le vide à jamais.
   * **La Conclusion** : Cette faille théorique prouve l'importance de ce que nous avons implémenté côté Akka : le renvoi explicite d'un message `StockInsuffisant` pour briser l'attente et éviter le deadlock dans la réalité.

---

## 🚀 4. Comment exécuter le projet et lancer les tests ?

### Prérequis
* Avoir installé **Java (JDK 11 ou plus)**
* Avoir installé **sbt** (Scala Build Tool). Dans Visual Studio Code, l'extension *Metals* peut s'en charger.

Ouvrez un terminal (`Terminal -> New Terminal` sous VS Code) et placez-vous dans le dossier du projet :
```bash
cd CY-Commerce
```

### Étape A : Lancer la Preuve Mathématique (Réseau de Pétri)
Executez la commande suivante pour lancer le moteur d'analyse :
```bash
sbt "runMain com.cycommerce.petrinet.EcommerceModel"
```
**Ce que vous allez voir :** Le parcours formel des 3 états du système et les preuves des invariants. La console va vous alerter ("DANGER LTL") sur le fait que le système mathématique pur risque un blocage si le stock est vide.

### Étape B : Lancer l'Application Akka
Executez la commande suivante pour jouer le scénario réel distribué :
```bash
sbt "runMain com.cycommerce.Main"
```
**Ce que vous allez voir :** L'échange de messages asynchrones. Alice recevra ses produits, le stock descendra, et l'Acteur Akka rejettera la commande de Bob en évitant brillamment le fameux "Deadlock" théorique calculé à l'étape A.

---

## 📝 5. Travail à faire pour le livrable (Le PDF Final)

Pour l'équipe, voici ce qu'il nous reste à rédiger dans le rapport final :
- [ ] **Sources LTL & Petri** : Trouver 2-3 liens bibliographiques qui définissent ce qu'est la logique temporelle et les réseaux de Pétri.
- [ ] **Dessin du Réseau** : Faire un schéma (sur Draw.io par exemple) de nos 3 Places (Ronds) reliées à la transition `T_ValiderCommande` (Rectangle). 
- [ ] **Intégration des Logs** : Copier-coller le résultat des terminaux A et B dans le rapport en expliquant que la théorie trouve un blocage, mais que notre conception Akka l'évite par l'envoi de messages compensatoires.