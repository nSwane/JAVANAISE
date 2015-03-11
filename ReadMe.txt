Nawaouis Swane
Giaccone Marc

Notre projet Javanaise est composé essentiellement de trois packages:

	- Le premiers (cs) contient le programme de lancement du coordinateur. On donne la possibilité à
l'utilisateur de libérer les ressources allouées par le coordinateur en l’arrêtant proprement. Dans
notre cas, le programme attend une commande afin de s’arrêter.

	- Le suivant (jvn) contient l'implémentation de Javanaise (implémentation, interface et utilitaires pour
la conception).

	- Le dernier (test) contient un ensemble de tests aléatoires qui nous a permis de stabiliser le projet.
Lancer la démo:

		* Lancer le coordinateur JvnCoordStarter.java.
		
		* Une fois le coordinateur prêt, les clients peuvent êtres lancé en exécutant Irc.java (le package
		irc_version1 contient la première version de Irc avec les JvnObjects, et le package irc_version2
		contient la version d'Irc avec les proxies dynamiques).
		
Lancer les tests :
	- Après avoir lancé le coordinateur, lancer TestStarter.java puis entrer le numéro du test à lancer(1
	ou 2) :
	
		➔ Test numéro 1: ce test simule l'utilisation du service Javanaise par plusieurs clients. Il s'agit
		d'un cas classique où les clients envoient une série de requêtes Read ou Write avant de se
		terminer correctement ou non.
		➔ Test numéro 2: test de reprise après panne. Le coordinateur est initialisé puis s'arrête
		brutalement pendant les transactions clientes avant de redémarrer. Le nombre de clients
		qu'on souhaite lancé n'est paramétrable que dans le code, il est fixé à 100 (100 JVM sont
		lancées pendant le test sans compter le coordinateur).

