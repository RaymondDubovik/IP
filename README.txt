//-----------------------------App&Backend-----------------------------\\
Open with Android Studio.
Gradle 2.10+ is required.
NB! During the development, Android Studio Beta 6 was used with beta dependencies. It is recommended to update to stable dependencies in build.gradle
Also, possibly build tools in build.gradle will need to be updated.

Contains 2 modules:
- android app
- Google App Engine backend server

1) Android app - easiest to launch from Android Studio.
- Depends on the backend module
- Modify ServerUrls.java to change the URL of the server that the app is connecting to.

2) Backend:
- Backend can be launched from Android Studio thanks to gradle plugin.
	If you wish to modify port or other launch options, modify it in the build.gradle
- Requires MySQL to run. MySQL details can be modified in WebApp/WEB-INF/mysql.config.properties. 
	If run locally, jdbc driver will be downloaded automatically thanks to gradle dependency
- Database schema can be found in schema.sql

	
//-----------------------------SummarisationServer-----------------------------\\	
Developed in Intellij IDEA.

Contains 2 modules:
- Categorizer
- Server


1) Categorizer
Needs to be trained, so it can categorize the news articles.
- To add new category, simply create a text file in trainingLinks directory
- Put links to the news articles of that category in file. One link per line
- If you wish to add more data, you can add links to the existing files
- Before categorizer can use new dataset, it needs to be trained. (specify -t as launch option)
- It will generate classifier.ser file, which is a serialized representation of the categorization object
- After it is trained with the new dataset, it can be evaluated
- It can be evaluated by adding a file with links to the news articles to the testingLinks directory. Name of the file must be the same as category that the news articles belong to (step similar to adding training data)
- Evaluation can be launched with -e launch option
- To add the categorizer to the summarization server, it needs to be exported as .jar archive
- .jar archive is generated as an Intellij artifact. Should be possible to add jar plugin to maven and export .jar archive or just do it from the terminal
- Categorizer FAQ:
	* Guess the category (best match only): 													-g "text" 
	* Categorize text (return ordered list of all categories with values of relevance): 		-c "text" 
	* Train categorizer with new data:			 												-t [ngram size]
	* Evaluate categorizer on test dataset: 													-e

2) Server
- .war archive generated as an artifact. Dependencies are managed with maven. So it should be possible to add war plugin to maven and export .war archive.
- Requires MEAD:
	* First run Install.PL in MEAD directory
	* /mead/bin/addons/formatting/Readme describes how the /mead/bin/addons/formatting/*.pm files can be added to the perl path
	* The approach described in that file didn't work for me, so I added them to the perl directory instead (/usr/lib/perl/_perl_version_here)
- Afeter mead is configured and categorization .jar and classifier.ser are present corerctly installedodify WebContent/config.properties with correct pathes
