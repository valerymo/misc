# Reali Engineering Problem - task Drfat

Suggested solution draft (coding not complete) is intended for processing Big Files.
Idea is following:
1. CSV File downloaded from Web and saved on disk
3. Processing BIG file: splitting it to small files, and run thread to process each small file.
	Not waiting for split all, but working as following:
	Read orig file, and write to new small file (where lines numvber is limited)
		If number of lines > MAX:
			complete write to current file
			Start processing current file (Thread)
			Create new file and continue write to it
			.....

TODO:
    csvLineToJson(String line)
	printJSON(...)

7. Helm for deployment - included
8. Dockerfile for image creation - included
