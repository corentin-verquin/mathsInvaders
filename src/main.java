//Corentin Verquin, Rodolphe Vanderaspoilden, copyright© 2016

import extensions.CSVFile;
class main extends Program{

    boolean modeMenu  = true; // true tant qu'on a pas choisi de boutton dans le menu
    boolean modeJouer = false;
    boolean modeClassement = false;
    boolean modeZoneProf = false;
    boolean modeMenuProf = false;
    boolean finDuJeu=false;

    // VARIABLES UTILE SEULEMENT POUR LE MENU
    
    char[] posCursorMenu = new char[]{'>',' ',' ',' '}; // permet de donner la pos du cursor
    int posInMenu = 0; // La position initiale du curseur est sur Jouer
    String[] listButtonMenu = new String[]{"Jouer","Classement","Zone professeur","Quitter"}; // liste des differents bouttons du menu
   

    // VARIABLES UTILE SEULEMENT POUR LE MENU PROF

    char[] posCursorMenuProf = new char[]{'>',' ',' '}; // permet de donner la pos du cursor
    int posInMenuProf = 0; // La position initiale du curseur est sur Soustraction
    String[] listButtonMenuProf = new String[]{"Soustraction","Multiplication ","Changer mot de passe"}; // liste des differents bouttons du menu
    boolean[] config = new boolean[2];
    boolean modif=false;

    // VARIABLES POUR JEUX

    String[][] zoneDeJeu = new String[11][20];
    int vie=8, score=0, positionChiffre=0;
    String vaisseau = "⌂", tir = "|";
    boolean finNiveau=false;
    int[] calculEnCour={0,0};
    boolean jeuInitialized = false;
    int tempsDeJeu=1700;

    void algorithm(){
	CSVFile classement;			
	int[][][] calcul=new int[4][10][3];
	int[][] operation=new int[3][10];
	long To;
	
	//test si les fichiers indispensable sont présent
	if(!presentFile()){
	    text("red");
	    println("\n\t!!! IMPOSSIBLE DE CHARGER LE JEU DES FICHIERS SONT MANQUANTS !!!\n");
	    reset();
	}else{	
	    classement=loadCSV("classement.csv");
	    initialiseConfig();	    

	    //continue le jeu
	    do{
		classement=loadCSV("classement.csv");
		
		enableKeyTypedInConsole(true); // On active la detection des touches du clavier
		// Gere le menu
		do{
		    afficherAccueil(); // Affiche le titre
		    afficherMenu();
		    delay(500);
		}while(modeMenu);

		//Gere le jeu
		while(modeJouer){
		    if(!jeuInitialized){
			initialiseConfig();
			positionChiffre=0;
			enableKeyTypedInConsole(true);
			initialiseCalc(calcul);
			initialiseOp(operation);
			initialiseZoneDeJeu(calcul);
			jeuInitialized = true;
			if(tempsDeJeu<=1200){
			    tempsDeJeu=tempsDeJeu-100;
			}
		    }
		    To=getTime();
		    while(getTime()-To < 10*tempsDeJeu && modeJouer  && !finNiveau){
			if(vie<=0){
			    affichageGameOver();
			    entreeScore(classement);
			}else{
			    afficherZoneDeJeu(calcul,operation);
			    affichageOperation(calcul,operation);
			}
			delay(100);
		    }
		    if(finNiveau){
			affichageNextLv();
		    }else if(positionChiffre!=7){
			descente();
			delay(100);
		    }else{
			affichageTimeOut();
			if(vie<=0){
			    affichageGameOver();
			    entreeScore(classement);
			}
		    }
		}

		//Gere le classement
		if(modeClassement){
		    afficherClassement(classement);
		}

		//Gere la zone professeur
		else if(modeZoneProf){
		    entreeProf();       
		}

	    }while(!finDuJeu);

	    //On termine le jeu
	    reset(); // On remet les couleurs par défaut
	    clearScreen();
	    cursor(0,0);
	    show(); // On affiche le curseur
	}
    }

    boolean presentFile(){
	String[] allDirectory=getAllFilesFromCurrentDirectory(); //recupere tout les fichiers
	boolean file1=false, file2=false, file3=false, file4=false, file5=false, file6=false, file7=false, file8=false, file9=false;
	for(int i=0; i<length(allDirectory); i++){ //test si les fichiers sont la ou non
	    if(equals(allDirectory[i],"accueil.csv")){
		file1=true;
	    }else if(equals(allDirectory[i],"classement.csv")){
		file2=true;						
	    }else if(equals(allDirectory[i],"mdp.csv")){
		file3=true;
	    }else if(equals(allDirectory[i],"config.csv")){
		file4=true;
	    }else if(equals(allDirectory[i],"laser.mp3")){
		file5=true;
	    }else if(equals(allDirectory[i],"loos.mp3")){
		file6=true;
	    }else if(equals(allDirectory[i],"win.mp3")){
		file7=true;
	    }else if(equals(allDirectory[i],"timeOut.mp3")){
		file8=true;
	    }else if(equals(allDirectory[i],"suppr.mp3")){
		file9=true;
	    }
	}
	if(!file1 || !file2 || !file3 || !file4 || !file5 || !file6 || !file7 || !file8 || !file9){
	    return false;
	}
	return true;
    }

    void keyTypedInConsole(char c){
    
	// Seulement pour le menu
	if(modeMenu){
	    cursor(17,0);
	    clearLine();
	    // Change la position du curseur

	    if(posInMenu < (length(listButtonMenu) - 1) && c == ANSI_DOWN){
		posInMenu = posInMenu + 1; // Descend le curseur au boutton ci-dessous
	    }else if(posInMenu == (length(listButtonMenu) - 1) && c == ANSI_DOWN){
		posInMenu = 0; // Revient au premier boutton
	    }
	
	    if(posInMenu > 0 && c == ANSI_UP){
		posInMenu = posInMenu - 1; // Remonte le curseur au boutton ci-dessus
	    }else if(posInMenu == 0 && c == ANSI_UP){
		posInMenu = (length(listButtonMenu) - 1); // Revient au dernier boutton
	    }

	    addCursorInMenu(posInMenu); // On remet '>' à la position voulue
	
	    // Si on appuie sur Entrer alors on arrete de capturer les touches
	   
	    if(c == 13){
		enableKeyTypedInConsole(false); // On desactive la detection des touches du clavier

		// On change le mode de jeu
		if(posInMenu == 0){
		    modeJouer= true;
		}else if(posInMenu == 1){
		    modeClassement = true;
		}else if(posInMenu == 2){
		    modeZoneProf = true;
		}else if(posInMenu == 3){
		    finDuJeu = true;
		}

		modeMenu = false;
	    }
	    //pour le menu prof
	}else if(modeMenuProf){
	    cursor(8,0);
	    clearLine();

	    if(posInMenuProf < (length(listButtonMenuProf) - 1) && c == ANSI_DOWN){
		posInMenuProf = posInMenuProf + 1;
	    }else if(posInMenuProf == (length(listButtonMenuProf) - 1) && c == ANSI_DOWN){
		posInMenuProf = 0;
	    }
	
	    if(posInMenuProf > 0 && c == ANSI_UP){
		posInMenuProf = posInMenuProf - 1;
	    }else if(posInMenuProf == 0 && c == ANSI_UP){
		posInMenuProf = (length(listButtonMenuProf) - 1);
	    }

	    addCursorInMenuProf(posInMenuProf);

	    if(c == 13){
		
		// On change les parametre
		if(posInMenuProf == 0){ //met a jour la soustraction
		    if(config[0]){
			config[0]=false;
		    }else{
			config[0]=true;
		    }
		}else if(posInMenuProf == 1){ //met a jour la multiplication
		    if(config[1]){
			config[1]=false;
		    }else{
			config[1]=true;
		    }
		}else if(posInMenuProf == 2){
		    modif=true;
		    enableKeyTypedInConsole(false);
		    modeMenuProf = false;
		}
		
	    }else if (c == 113){// quitter avec q
		enableKeyTypedInConsole(false);
		modeMenuProf = false;
	    }
	    
	}else{//action clavier en jeu
	    cursor(23,0);
	    clearLine();
	    if(c == ANSI_RIGHT){
		playerAction("moveRight");
	    }else if(c == ANSI_LEFT){
		playerAction("moveLeft");
	    }else if(c == 32){ // Space
		playSound("laser.mp3");
		playerAction("shoot");
	    }else if(c == 113){// Quitter avec q
	        enableKeyTypedInConsole(false);
		modeMenu = true;
		jeuInitialized = false;
		modeJouer = false;
		positionChiffre=0;
		calculEnCour[0]=0;
		calculEnCour[1]=0;
		vie=8;
		tempsDeJeu=1700;
		score=0;
	    }
	}
    }

    void playerAction(String action){//definit les actions du joueur
	boolean movementDone = false;
	
	for(int l = 0; l < length(zoneDeJeu,1); l++){
	    for(int c = 0; c < length(zoneDeJeu,2); c++){
		if(zoneDeJeu[l][c] == vaisseau && !movementDone){
		    if(equals(action,"moveRight") && (c+1 <= (length(zoneDeJeu,2) - 1))){//deplace le vaisseau a droite
		        if(c%2==0){//pour rester alligné au chiffre
			    zoneDeJeu[l][c] = "   ";
			}else{
			    zoneDeJeu[l][c] = " ";
			}
			zoneDeJeu[l][c+1] = vaisseau;
		    }else if(equals(action,"moveLeft") && (c-1 >= 0)){//deplace le vaisseau a gauche
			if(c%2==0){//pour rester alligné au chiffre
			    zoneDeJeu[l][c] = "   ";
			}else{
			    zoneDeJeu[l][c] = " ";
			}
			zoneDeJeu[l][c-1] = vaisseau;
		    }else if(equals(action,"shoot")){//lance le tire
			zoneDeJeu[l-1][c] = tir;
		    }
		    movementDone = true;//test si l'action a ete faite
		}
	    }
	}
    }
   
    //ensemble des fonctions d'initialisation

    void initialiseZoneDeJeu(int[][][]tab){
	for(int i=0; i<length(tab,3); i++){//place les chiffres
	    for(int j=0; j<length(tab,2); j++){
		if(length(""+tab[3][j][i])==1){//pour que tout soit alligne
		    zoneDeJeu[i][j*2]=" 0"+tab[3][j][i];
		}else{
		    zoneDeJeu[i][j*2]=" "+tab[3][j][i];
		}
	    }
	}

	for(int i=0; i<3; i++){//place le vide entre les chiffres
	    for(int j=1; j<=length(zoneDeJeu,2); j=j+2){
		zoneDeJeu[i][j]=" ";
	    }
	}
        
	for(int i=3; i<length(zoneDeJeu,1); i++){//place le vide dans le reste du tableau
	    for(int j=0; j<length(zoneDeJeu,2); j++){
		if(j%2==0){//pour rester alligné aux chiffres
		    zoneDeJeu[i][j]="   ";
		}else{
		    zoneDeJeu[i][j]=" ";
		}
	    }
	}
	zoneDeJeu[10][(int) (random()*19)]=vaisseau;//place le vaiseau aleatoirement
	
    }
    
    void initialiseCalc(int[][][] tab){//permet de genere est stocker des calculs aleatoires
	int r=0, tampon=0;
	for(int p=0; p<length(tab,3); p++){
	    for(int c=0; c<length(tab,2); c++){
		r=(int) (random()*10);//premier operande
		tab[0][c][p]=r;
		r=(int) (random()*3);//operateur 0= * 1= - 2= +
		tab[1][c][p]=r;
		r=(int) (random()*10);//second operande
		tab[2][c][p]=r;

		if(tab[1][c][p]==0 && config[1]==false){
		    tab[1][c][p]=3;
		}

		if(tab[1][c][p]==1 && config[0]==false){
		    tab[1][c][p]=3;
		}

		//effectue le calcule et stock le resultat
		if(tab[1][c][p]==0 && config[1]==true){//multiplication
		    tab[3][c][p]=tab[0][c][p]*tab[2][c][p];
		}else if(tab[1][c][p]==1 && config[0]==true){//soustraction
		    if(tab[0][c][p] > tab[2][c][p]){ //pour eviter les resultats négatifs
			tab[3][c][p]=tab[0][c][p]-tab[2][c][p];
		    }else{
			tampon=tab[0][c][p];
			tab[0][c][p]=tab[2][c][p];
			tab[2][c][p]=tampon;
			tab[3][c][p]=tab[0][c][p]-tab[2][c][p];
		    }
		}else{//addition
		    tab[3][c][p]=tab[0][c][p]+tab[2][c][p];
		}
	    }
	}
    }

    void initialiseOp(int[][] tab){
	//permet de tirer les operation aleatoirement pour la suite du jeu
	int[] alleatoire={0,1,2,3,4,5,6,7,8,9};
	int r=0, tampon=0;
	for(int l=0; l<length(tab,1); l++){
	    for(int c=0; c<length(tab,2); c++){
		r=(int) (random()*(length(alleatoire)-1-c));
		tampon=alleatoire[r];
		tab[l][c]=alleatoire[r];
		alleatoire[r]=alleatoire[length(alleatoire)-1-c];
		alleatoire[length(alleatoire)-1-c]=tampon;
	    }
	}
    }

    void initialiseConfig(){//modifie les parametres du jeu
	if(equals(getCell(loadCSV("config.csv"),1,0),"1")){ //soustraction
	    config[0]=true;
	}else{
	    config[0]=false;
	}

	if(equals(getCell(loadCSV("config.csv"),1,1),"1")){//multiplication
	    config[1]=true;
	}else{
	    config[1]=false;
	}
    }

    //ensemble des fonctions d'affichage
  
    void afficherMenu(){
	for(int i = 0; i < length(listButtonMenu); i++){
	    cursor(13+i,20);
	    println(posCursorMenu[i]+" "+listButtonMenu[i]);
	}
    }

    void afficherAccueil(){//affiche l'ascii art de l'accueil situe dans un csv
	hide();
	background("black");
	clearScreen();
	text("yellow");
	CSVFile titre=loadCSV("accueil.csv");
	for(int i=0; i<10; i++){//affiche le csv
	    cursor(1+i,20);
	    println(getCell(titre,i,0));
	}
	text("green");
	print("\n\n");
    }

    void afficherClassement(CSVFile classement){//permet d'afficher le classement
	clearScreen();
	cursor(1,35);
	text("yellow");
	print("Classement");
	cursor(2,31);
	print("------------------\n\n\n");
	for(int c=0; c<columnCount(classement); c++){//affiche le csv
	    cursor(5+c,30);
	    if(!equals(getCell(classement,0,c),"empty")){
		cursor(5+c,30);
		print((c+1)+" : "+getCell(classement,0,c));
		cursor(5+c,50);
		print(getCell(classement,1,c)+"\n");
	    }
	}
	delay(7000);
	modeClassement=false;
	modeMenu=true;
    }
    
    void afficherProf(){//affiche les parametres
	clearScreen();
	String[][] savMotDePasse=new String[][]{{"mot de passe"},{""}};
	modeMenuProf=true;
	enableKeyTypedInConsole(true);
	do{
	    interfaceProf();
	    afficherMenuProf();
	    delay(500);
	}while(modeMenuProf);//tant que l'on est dans le menu prof
	if(modif){//si on veut modifier le mot de passe
	    clearScreen();
	    cursor(12,10);
	    print("Veuillez choisir un mot de passe : ");
	    savMotDePasse[1][0]=crypto(readString());
	    saveCSV(savMotDePasse,"mdp.csv");//enregistre le nouveau mot de passe
	    modif=false;
	}
	saveConfig();
	modeZoneProf=false;
	modeMenu=true;
    }

    void interfaceProf(){//permet de mettre en place l'interface prof
	clearScreen();
	cursor(1,32);
	print("Zone professeur");
	cursor(2,31);
	print("------------------\n\n\n");
	 
	cursor(5,50);
	if(config[0]){//changement de couleur selon active ou non
	    text("green");
	    print("oui");
	}else{
	    text("red");
	    print("non");
	}

	cursor(6,50);
	if(config[1]){//changement de couleur selon active ou non
	    text("green");
	    print("oui");
	}else{
	    text("red");
	    print("non");
	}	    

	text("green");
	cursor(10,27);
	print("Appuyez sur q pour quitter");	
 
	text("yellow");
    }
    
    void afficherMenuProf(){//affiche les choix possibles pour le prof
	for(int i = 0; i < length(listButtonMenuProf); i++){
	    cursor(5+i,25);
	    println(posCursorMenuProf[i]+" "+listButtonMenuProf[i]);
	}
    }

    void afficherZoneDeJeu(int[][][] calcul, int[][]operation){
	clearScreen();
	text("yellow");
	cursor(1,0);
	println("Utilise les fleches ← →  pour te déplacer");
	println("Et la barre d'espace ▭  pour tirer");
	print("La touche Q sert à quitter le jeu");
	text("green");
	String[][] color={{"blue","red","blue","yellow","green","purple","red","blue","yellow","green"},{"red","red","purple","yellow","blue","green","purple","yellow","blue","red"},{"green","yellow","blue","green","purple","blue","red","green","blue","yellow"}};
	for(int i=0; i<length(zoneDeJeu,1); i++){
	    cursor(6+i,17);
	    print("▓");//dessine le cadre
	    cursor(6+i,20);	    
	    for(int j=0; j<length(zoneDeJeu,2); j++){
		//affiche les chiffres avec les couleurs de color
		if(i<positionChiffre+3 && i>=positionChiffre && j%2==0 && ! equals(zoneDeJeu[i][j],tir+"  ")){
		    text(color[i-positionChiffre][j/2]);
		    print(zoneDeJeu[i][j]);
		}else{//sinon si ce n'est pas les chiffres
		    text("green");
		    print(zoneDeJeu[i][j]);
		}
		if(equals(zoneDeJeu[i][j],tir) || equals(zoneDeJeu[i][j],tir+"  ")){//test si la case contient le tir
		    //la case actuelle devient vide
		    if(j%2==0){
			zoneDeJeu[i][j] = "   ";//pour rester alligne au chiffre
		    }else{
			zoneDeJeu[i][j] = " ";
		    }
		    if(i-1 >= 0){
			if(equals(zoneDeJeu[i-1][j]," ") || equals(zoneDeJeu[i-1][j],"   ")) {//test si la case suivante est vide
			    //si oui la case suivante devient tir
			    if(j%2==0){
				zoneDeJeu[i-1][j] = tir+"  ";
			    }else{
				zoneDeJeu[i-1][j] = tir;
			    }
			}else{//sinon test si le resultat est le bon
			    calculCorect(calcul,operation,i-1,j);
			}
		    }
		}
	    }
	    cursor(6+i,62);
	    print("▓");//dessine le cadre
	}
	
	for(int i=17; i<63; i++){//dessine le cadre
	    cursor(5,i);
	    print("▓");
	    cursor(17,i);
	    print("▓");
	}

	//dessine les informations complementaires
	text("yellow");
	cursor(21,20); print("Nombre de vie : ");
	for(int i=0; i<vie; i++){//vie
	    text("red");
	    print("♥ ");
	}
	text("yellow");//score
	cursor(22,20); print("Score : "+score+"\n");
    }
    
    void affichageOperation(int[][][] calc, int[][] op){//permet d'afficher les opérations dans un ordre accessible
	int index=op[calculEnCour[0]][calculEnCour[1]];
	cursor(19,20);
	print(calc[0][index][length(op,1)-1-calculEnCour[0]]);
	if(calc[1][index][length(op,1)-1-calculEnCour[0]]==0){
	    print(" x ");
	}else if(calc[1][index][length(op,1)-1-calculEnCour[0]]==1){
	    print(" - ");
	}else{
	    print(" + ");
	}
	print(calc[2][index][length(op,1)-1-calculEnCour[0]]+" ?");
	cursor(23,20);
    }
	
    void affichageTimeOut(){//affiche que le temps est fini
	playSound("timeOut.mp3");
	enableKeyTypedInConsole(false);
	clearScreen();
	cursor(10,19);
	delay(500);
	print("Tu n'as pas été assez rapide tu perds 2 vies");
	delay(4000);
	jeuInitialized = false;
	calculEnCour[0]=0;
	calculEnCour[1]=0;
	positionChiffre=0;
	vie=vie-2;
    }
    
    void affichageNextLv(){//affiche et permet de passer au niveau suivant
	enableKeyTypedInConsole(false);
	finNiveau=false;
	jeuInitialized = false;
	calculEnCour[0]=0;
	calculEnCour[1]=0;
	playSound("win.mp3");
	clearScreen();
	cursor(9,9);
	delay(500);
	print("Felicitation tu as passé un niveau. Multiplication du score");
	cursor(10,9);
	print("                   Ancien score : "+score);
	cursor(11,9);
	score=(int) (score*1.15);
	print("                  Nouveau score : "+score);
        delay(4000);    
    }
    
    void affichageGameOver(){//affiche la fin du jeu
	playSound("loos.mp3");
	enableKeyTypedInConsole(false);
	clearScreen();
	cursor(10,23);
	delay(500);
	print("Tu n'as plus de vie pour continuer");
	cursor(11,23);
	print("           Score : "+score);
	delay(4000);
	modeMenu = true;
	jeuInitialized = false;
	modeJouer = false;
	calculEnCour[0]=0;
	calculEnCour[1]=0;
	vie=8;
	tempsDeJeu=1700;
    }
	
    void entreeProf(){//permet d'aller dans les parametres
	clearScreen();
      	text("yellow");
	CSVFile mdp=loadCSV("mdp.csv");
	String motDePasse=getCell(mdp,1,0);
	String[][] savMotDePasse=new String[][]{{"mot de passe"},{""}};
	if(equals(motDePasse,"0")){//si il n'y a pas de mot de passe
	    cursor(12,10);
	    print("Veuillez choisir un mot de passe : ");
	    savMotDePasse[1][0]=crypto(readString());
	    saveCSV(savMotDePasse,"mdp.csv");
	    afficherProf();
	}else{//sinon demande le mot de passe
	    cursor(12,10);
	    print("Veuillez entrer le mot de passe : ");
	    text("black");
	    show();
	    if(equals(crypto(readString()),motDePasse)){//si le mot de passe est correcte alors -> aller dans parametre
		text("yellow");
		hide();
		afficherProf();
	    }else{//sinon retour accueil
		hide();
		text("red");
		cursor(14,10);
		print("Mot de passe incorect");
		delay(4000);
		modeZoneProf=false;
		modeMenu=true;
	    }
	}
    }    

	
    // Fonction qui permet de parcourir le tableau de caracteres et mettre '>' a la bonne position
    void addCursorInMenu(int pos){
	for(int i = 0; i < length(posCursorMenu); i++){
	    if(i != pos){
		posCursorMenu[i] = ' ';
	    }else{
		posCursorMenu[i] = '>';
	    }
	}
    }

    void addCursorInMenuProf(int pos){//permet de bouger '>'
	for(int i = 0; i < length(posCursorMenuProf); i++){
	    if(i != pos){
		posCursorMenuProf[i] = ' ';
	    }else{
		posCursorMenuProf[i] = '>';
	    }
	}
    }
    
    String crypto(String s){//permet de crypter le mot de passe
	int acumulateur=0;
	for(int i=0; i<length(s); i++){
	    acumulateur=acumulateur+(int) charAt(s,i);
	}
	acumulateur=acumulateur*2+4;
	return acumulateur+"";
    }

    void saveConfig(){//sauvegarde les parametre dans le csv
 	String[][] sConfig=new String[][]{{"soustraction","multiplication"},{"",""}}; //sert pour sauver la config
	if(config[0]){
	    sConfig[1][0]="1";
	}else{
	    sConfig[1][0]="0";
	}
	if(config[1]){
	    sConfig[1][1]="1";
	}else{
	    sConfig[1][1]="0";
	}
	
	saveCSV(sConfig,"config.csv");
    }
    
    void calculCorect(int[][][] calc, int[][] op, int i, int j){//permet de savoir si le joueur a choisi le bon resultat
	int index=op[calculEnCour[0]][calculEnCour[1]];
	int operande1 = calc[0][index][length(op,1)-1-calculEnCour[0]];
       	int operande2 = calc[2][index][length(op,1)-1-calculEnCour[0]];
	int choixJoueur = stringToInt(sansEspace(zoneDeJeu[i][j]));
	boolean corect = false;
	int operateur=calc[1][index][length(op,1)-1-calculEnCour[0]];     
	if(operateur==0){
	    if(operande1*operande2==choixJoueur){
		corect=true;
	    }
	}else if(operateur==1){
	    if(operande1-operande2==choixJoueur){
		corect=true;
	    }
	}else{
	    if(operande1+operande2==choixJoueur){
		corect=true;
	    }	
	}
	if(corect){//augmente le score et efface le resultat correcte
	    zoneDeJeu[i][j]="   ";
	    score=score+100;
	    if(calculEnCour[1]<9){//permet de passer au calcul suivant
		calculEnCour[1]=calculEnCour[1]+1;
	    }else if (calculEnCour[0]<2){
		calculEnCour[1]=0;
		calculEnCour[0]=calculEnCour[0]+1;
	    }else{//test si le niveau est finit
		finNiveau=true;
	    }
	    playSound("suppr.mp3");
	}else{//diminue la vie
	    vie=vie-1;
	}
    }
    
    String sansEspace(String s){//supprime les espaces avant le chiffre
	while((charAt(s,0)==' ' || charAt(s,0)=='0') && !equals(s,"0")){
	    s=substring(s,1,length(s));
	}
	return s;
    }

    void descente(){//descent les chiffres
	for(int i=positionChiffre+3; i>positionChiffre; i--){//copie les chiffres vers le bas
	    for(int j=0; j<length(zoneDeJeu,2); j++){
		zoneDeJeu[i][j]=zoneDeJeu[i-1][j];
	    }
	}
	for(int i=0; i<=positionChiffre; i++){//remplace le haut par des " "
	    for(int j=0; j<length(zoneDeJeu,2); j++){
		if(j%2==0){
		    zoneDeJeu[i][j]="   ";
		}else{
		    zoneDeJeu[i][j]=" ";
		}
	    }
	}
	positionChiffre=positionChiffre+1;
    }
    
    void entreeScore(CSVFile classement){//test si le score peut entrer dans le classement
	String[][]tab=new String[2][10];
	for(int i=0; i<rowCount(classement); i++){
	    for(int j=0; j<columnCount(classement); j++){
		tab[i][j]=getCell(classement,i,j);
	    }
	}
	String prenom;
	if(score>=stringToInt(tab[1][length(tab,2)-1])){
	    show();
	    clearScreen();
	    cursor(10,0);
	    print("Tu as battu un des meilleurs score. Entre ton prénom : ");
	    prenom=readString();
	    if(equals(prenom,"")){
		prenom="anonyme";
	    }
	    trie(score,prenom,tab);
	    hide();
	    score=0;
	    saveCSV(tab,"classement.csv");
	}
    }

    void trie(int n, String s, String[][] tab){//effectue le trie a bulle
	String tamponNom, tamponScore;
	boolean permut;
	tab[0][length(tab,2)-1]=s;
	tab[1][length(tab,2)-1]=""+n;
	do{
	    permut=false;
	    for(int i=0; i<length(tab,2)-1; i++){
		if(stringToInt(tab[1][i]) < stringToInt(tab[1][i+1])){
		    tamponNom=tab[0][i];
		    tamponScore=tab[1][i];
			
		    tab[0][i]=tab[0][i+1];
		    tab[1][i]=tab[1][i+1];
			
		    tab[0][i+1]=tamponNom;
		    tab[1][i+1]=tamponScore;

		    permut=true;
		}
	    }
	}while(permut);
    }

    //ensemble des fonctions test

    void testTrie(){
        String[][]principal={{"A","B","C","D","E","F","G","H","I","J"},{"1000","999","800","550","549","300","200","100","50","10"}};
      	final String[][]test1={{"T1","A","B","C","D","E","F","G","H","I"},{"1500","1000","999","800","550","549","300","200","100","50"}};
      	final String[][]test2={{"T1","A","B","C","D","E","T2","F","G","H"},{"1500","1000","999","800","550","549","400","300","200","100"}};
      	final String[][]test3={{"T1","A","B","C","D","E","T2","F","G","T3"},{"1500","1000","999","800","550","549","400","300","200","150"}};
	trie(1500,"T1",principal);
       	assertArrayEquals(test1,principal);
	trie(400,"T2",principal);
       	assertArrayEquals(test2,principal);
	trie(150,"T3",principal);
      	assertArrayEquals(test3,principal);
    }


    void testSansEspace(){
	assertEquals("1",sansEspace("    1"));
	assertEquals("2  ",sansEspace("    2  "));
	assertEquals("3",sansEspace(" 03"));
	assertEquals("0",sansEspace(" 00"));
    }

    void testCrypto(){
	assertEquals("1538",crypto("bonjour"));
	assertEquals("422",crypto("1538"));
	assertEquals("402",crypto("cd"));
	assertEquals("304",crypto("402"));
    }
    
}

    

