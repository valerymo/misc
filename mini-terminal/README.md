
# Mini-terminal

## Table of content
* [Quick Start](#quick-start)
* [Commands](#commands)
* [Configuration](#configuration)
* [Logging](#logging)
* [Planned improvements](#planned-improvements)


## About Miniterminal

Miniterminal - is a simple terminal implemented in Python, as a test task.
It allow execution of several commands, including batch commands script execution from script.
Json file is used for input and command parameters configuration.
Tools is easy for use, just need run it (python miniterminal.py) and print something in prompt - the list of available commands will appear. Usage prints will assist to start with each command.
Detailed notes with usage examples will be found in sections below.
Script is a sample for automation of commands execution.  
*Some improvements are planned, see: [Planned improvements](#planned-improvements)*

## Quick Start

Below are log to demonstrate - how to start work with miniterminal, and find commands usage examples.

    $ python miniterminal.py
    Welcome! Type ? to list commands
    mini> ?

    Documented commands (type help <topic>):
    ========================================
    EOF  clean  exit  help  script  sort  stat

    mini> ss
    Available commands: clean, sort, stat, script
    mini> clean
    USAGE: clean <path/to/folder>
    mini> sort
    USAGE: sort <path/to/folder> --hash=<path/to/hash>
    mini> stat
    USAGE: stat <path/to/folder> --csv=<path/to/hash> --ts=<timestamp>
    mini>
    mini> q
    Bye
    $

Script command does not require command line arguments.  
It's reading parameters from file named input.json located at working directory.


## Commands
Miniterminal supporting the following commands:  

* sort  
* clean   
* stat  
* script  
    


### Sort

#### Command description

USAGE: sort <path/to/folder> --hash=<path/to/hash>

`sort` command take a specific directory and sort its contents based on file type, 
        while creating a folders per file types .csv, .mat, .dxl. 
        Running the command will result into 3 subfolders within the parent one, 
        each has the name of files type and collects all of the files of this specific type. 
        Moreover, the command will provide a hash table (file) of how many files are 
        processed based on type.

Note. Current implementation supporting 3 file types (.csv, .mat, .dxl).
     Improvements planned: make file types configurable, using input.json
     See [Planned improvements](#planned-improvements)


**Input parameters:**  

* <path/to/folder> -- is a directory that contains files to be sorted.  
* --hash=<path/to/hash - hash table file to hold the info of
    how many files processed of each type  
    
**Output:**

* three subfolders created: dir/csv, dir/mat, dir/dxl
* files are moved to subfolders, each subforlder conatains files of one type, 
corresponding to subforlder name
* log file created in directory ./logs. See [Logging](#logging) for more info.  
In case of error, log file contains error description. 


#### Usage examples
**Correct sort command execution**

Before running command - Directory with files exists as following:

    $ ls  test/  
    f1.csv  f1.dxl  f1.mat  f2.csv  f2.mat  f3.csv

Run sort:  
 
    $ python miniterminal.py  
    Welcome! Type ? to list commands  
    mini> sort test --hash=f1  
    mini> q  
    Bye  

Results:  

    $ find test/
    test/
    test/csv
    test/csv/f1.csv
    test/csv/f2.csv
    test/csv/f3.csv
    test/dxl
    test/dxl/f1.dxl
    test/f1
    test/mat
    test/mat/f1.mat
    test/mat/f2.mat

Hash file created:

    $ cat test/f1
    csv: 3
    mat: 2
    dxl: 1
*Note. TODO - f1 file need to be created in working dir . and not in test (to fix))
See [Planned improvements](#planned-improvements)*

Log file created:

    $ cat logs/sort.log.1613824157
    Command: Sort
    Time: 1613824157
    Status: True
    csv: 3
    mat: 2
    dxl: 1


**Incorrect sort execution - too many or missing argiment example**

    $ python miniterminal.py
    Welcome! Type ? to list commands
    mini> sort
    USAGE: sort <path/to/folder> --hash=<path/to/hash>
    mini> sort 1 2 3
    USAGE: sort <path/to/folder> --hash=<path/to/hash>
    mini> q
    Bye

Logs:

    $ cat logs/sort.log.1613825613
    Command: Sort
    Time: 1613825613
    Status: False
    Error: Wrong parameters


### Clean

#### Command description

USAGE: clean <path/to/folder>

`clean` * This command needs to keep a specific window (number of files) in the log path. 
If they are higher, a number (deletion_threshold - more in the configuration part) 
of oldest logs will be deleted. The command takes the directory as an input.

**Input parameters:**  

* <path/to/folder> -- working directory path, where logs dir is located.  

* Configuration - MAX_NUM_OF_LOG_FILES defined in input.json - 
    see [Configuration](#configuration)
    
**Result:**
* Old log files removed from logs dir / window is maintained.


#### Usage examples

Logs dir before clean:  

    $ ls logs/
    clean.log.1613813471   sort.log.1613813471  sort.log.1613818030  sort.log.1613825620
    clean.log.1613818026   sort.log.1613813625  sort.log.1613824157  stat.log.1613813471
    script.log.1613813471  sort.log.1613813633  sort.log.1613825613  stat.log.1613818036


Clean:

    $ python miniterminal.py
    Welcome! Type ? to list commands
    mini> clean .
    mini> q
    Bye

Logs dir after clean:


    $ ls logs/
    clean.log.1613826979  sort.log.1613824157  sort.log.1613825620
    sort.log.1613818030   sort.log.1613825613  stat.log.1613818036


## Stat

#### Command description

USAGE: stat <path/to/folder> --csv=<path/to/hash> --ts=<timestamp>

* stat: The command will check your log folder, it takes a timestamp as an input, 
    and all logs created after this timestamp will be included in a statistics form. 
    This command scans the path and provides statistics based on a specific time frame
    as follows:
    * The command that failed the most. 
    * Most frequently used command. 
    * Least frequently used command.
The output - is a csv file that shows the mentioned stat points

**Input parameters:**  

* <path/to/folder> -- working directory path, where logs dir is located.  

* --csv=<path/to/hash> - path to csv file that will contains statistics info
* --ts=<timestamp>
 
 
**Result:**

* Statistics added to stat.csv file (*stat.csv* file created if not exists, or updated)


#### Usage examples

    mini> stat . --csv=. --ts=1613818026

This command will look at log dir *./logs*, and check file created after time provided
 with param *--ts*.  
 Statistics will be added to file *stat.csv*.  
 If file not exists -it will be created.
 
 **stat.csv** file example:
 
    $ cat stat.csv
    Timestamp,Most used,Least used,Most failed
    1613827988,sort,script,sort
    1613828384,sort,script,sort
    1613828385,sort,script,sort
    1613828386,sort,script,sort
    1613828386,stat,script,sort
    1613828387,stat,script,sort

## Script

#### Command description

USAGE: script

**script** command is using for automation demonstration. 
It's executing all three commands *clean, sort, stat* with parameters defined in **input.json** file.
*The command does not require commmand line arguments*, but using input.json file to retrieve parameters from there.
The result of the command execution - are set of results from *clean, sort, stat*. 
Log file created for script command.

**Input parameters:**  
./input.json file -- see [Configuration](#configuration) for example

 
**Result:**
- clean done
- sorting done
- stat done

#### Usage examples

    mini> script

Before running command:
    * Directory with files exists as following:

    $ ls  test/  
    f1.csv  f1.dxl  f1.mat  f2.csv  f2.mat  f3.csv

    * logs dir empty
    * stat.csv file doesn't exists

Run command:  

    mini> script

Results:
    Clean, Sort and Stat - done:
    

    $ find test/
    test/
    test/csv
    test/csv/f1.csv
    test/csv/f2.csv
    test/csv/f3.csv
    test/dxl
    test/dxl/f1.dxl
    test/f1
    test/mat
    test/mat/f1.mat
    test/mat/f2.mat

    $ ls logs/
    clean.log.1613830088  script.log.1613830088  sort.log.1613830088  stat.log.1613830088

    $ cat logs/*
    Command: Clean
    Time: 1613830088
    Status: True
    Command: Script
    Time: 1613830088
    Status: True
    Command: Sort
    Time: 1613830088
    Status: True
    csv: 3
    mat: 2
    dxl: 1
    Command: Stat
    Time: 1613830088
    Status: True


    $ cat stat.csv
    Timestamp,Most used,Least used,Most failed
    1613830088,sort,stat,

### Conditional execution of the commands in Script

In current design the functionality for conditional execution of commands in script
 could be achived by cleaning specific command parameters.
For example, for Clean command: *{ "clean_command_args":""}*
But it impacts on statistics, as command will be considered as failed, 
and log will be created with failed status, as following:
  
    Status: False
    Error: Wrong parameters

So we suppose that need add enhancement - functionality for conditional execution of commands in script,
 although it's not signed directly in requirements.
Changes will be required in *input.json* file and in *ScriptProcessor* class.
Notes added to [Planned improvements](#planned-improvements) section





## Configuration

Below is sample of input.json  - Configuration file.


    {
      "MAX_NUM_OF_LOG_FILES": "5",
      "SCRIPT":[
          { "clean_command_args":"."},
          { "sort_command_args":"test --hash=f1"},
          { "stat_command_args":". --csv=. --ts=1500000000"}]
    }

MAX_NUM_OF_LOG_FILES - is a "window_size" is the number of files that *clean* command will save in log directory.

Note. Regarding requirement to support csv and log "log_type" - see: [Logging](#logging) and [Planned improvements](#planned-improvements)


## Logging

Log files samples:

    $cat stat.log.1613813471
    Command: Stat
    Time: 1613813471
    Status: True

    $ cat sort.log.1613818030
    Command: Sort
    Time: 1613818030
    Status: False
    Error: Wrong parameters


Currently *.log* format is supported by all commands.  
In addition - CSV file is created by *stat* command; it conains logs analyses info.

See also [Planned improvements](#planned-improvements), 
maybe need clear requirements regarding supported log_types.


## Planned improvements

These are planning enhansments, known issues, and open items.

1. Discuss - Make file types that processed by Sorting operation -  to be configurable, using input.json.  
    Current implementation supports 3 predefined file types (.csv, .mat, .dxl).   
2. Create Hash file in working dir instead of directory where files located
3. Discuss - Configurable "log_type", if required.
CSV file created  only by *stat* command. 
4. Discuss - Script command implemented without usage any command line parameter, as it's using input.json. 
   Maybe need clear requiremenets.
5. Add conditional execution of the commands in the script.  
	Update input.json, - add *Run* parameter (Y/N) for each command.
	Update ScriptProcessor class.