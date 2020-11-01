# Reali Engineering Problem - task Drfat

Suggested solution draft (coding not complete) is intended for processing Big Files.
Idea is following:
1. CSV File downloaded from Web (SpringBoot REST)
2. File saved on disk
3. Checking if file is Big (Max number of lines defined)
4. If file is Big - it's splitted to several  small files
5. Each Small file - processed line by line
	Actually, as I see maybe even no needed split (as it also took time),
	as finally the approach for file processing is for Big files, read line by line. Need recheck ...
6. Not completed - creation Json Object

Notes. solution for Big Data need work with DB, as The Collection of Json records could be also huge. Or probably it should ve return as events.

7. Helm for deployment - included
8. Dockerfile for image creation - included

Thank you Reali for interesting Task,
Unfortunately - not enough time for me.
Thanks & Regards,
Valery