#!/usr/bin/python

from cmd import Cmd
import csv
import json
import logging
import operator
import os
import shutil
import time

logging.basicConfig(level=logging.INFO)
INPUT_FILE = "input.json"


def main():
    logging.debug("main")
    MiniTerminal().cmdloop()


class Utils:
    def __init__(self):
        logging.debug("class Utils")

    def check_folder_exists(self, path):
        full_dir_path = path
        if path.find(".", 0) or (path.find("/", 0) == -1):
            logging.debug("Relative path, adding current dir")
            full_dir_path = os.path.abspath(os.getcwd()) + os.path.sep + path
        logging.debug("full_dir_path: " + full_dir_path)
        if not os.path.exists(full_dir_path) or not os.path.isdir(full_dir_path):
            logging.error("Directory Path does not exists: " + full_dir_path)
            return False
        else:
            return True

    def logs(self, command, status, error, count_csv=0, count_mat=0, count_dxl=0):
        # logfile located in current dir - where script was executed from
        ts = str(time.time()).split(".")[0]
        log_dir_path = os.getcwd() + os.path.sep + "logs"
        if not os.path.exists(log_dir_path):
            os.mkdir(log_dir_path)
        logfile = log_dir_path + os.path.sep + command.lower() + ".log." + ts
        f = open(logfile, "w+")
        f.write("Command: " + command + "\n")
        f.write("Time: " + ts + "\n")
        f.write("Status: " + str(status) + "\n")
        if status and (command == "Sort"):
            f.write("csv: " + str(count_csv) + "\n")
            f.write("mat: " + str(count_mat) + "\n")
            f.write("dxl: " + str(count_dxl) + "\n")
        if not status:
            f.write("Error: " + error + "\n")
        f.close()

    def sorted_ls(self, path):
        mtime = lambda f: os.stat(os.path.join(path, f)).st_mtime
        return list(sorted(os.listdir(path), key=mtime))


class CleanProcessor:
    def __init__(self, input_params):
        logging.debug("class CleanProcessor")
        self.input_params = input_params
        self.utils = Utils()

    def help(self):
        command = "USAGE: clean <path/to/folder>"
        print(command)

    def clean(self, args):
        logging.debug("CleanProcessor.clean()")
        try:
            logging.debug("args: " + args)
            valid, logdir_path = self.process_args(args)
            if not valid:
                return
            self.check_files_limit_remove_oldest(logdir_path)
            self.utils.logs("Clean", True, "")
        except AssertionError:
            logging.error("CleanProcessor.clean:")
            self.utils.logs("Clean", False, "Error in CleanProcessor.clean()")

    def process_args(self, argsline):
        logging.debug("CleanProcessor.process_args()")
        args_list = argsline.split()
        logging.debug("args_list: " +  str(args_list))
        if (len(args_list) != 1):
            self.help()
            self.utils.logs("Clean", False, "Wrong parameters")
            return 0,0
        else:
            logdir_path = args_list[0] + os.path.sep + "logs" + os.path.sep
            logging.debug("logdir_path: " + logdir_path)
            if (self.utils.check_folder_exists(logdir_path) == False):
                self.utils.logs("Clean", False, "Directory does not exists: " + logdir_path)
                return 0,0
            return 1, logdir_path

    def check_files_limit_remove_oldest(self, logdir_path):
        logging.debug("CleanProcessor.check_threshhold_and_remove_oldest()")
        logging.debug("logdir_path: " + str(logdir_path))
        max_num_files = int(self.input_params.get('MAX_NUM_OF_LOG_FILES'))
        del_list = self.sorted_ls(logdir_path)[0:(len(self.sorted_ls(logdir_path)) - max_num_files)]
        for dfile in del_list:
            os.remove(logdir_path + dfile)

    def sorted_ls(self, path):
        mtime = lambda f: os.stat(os.path.join(path, f)).st_mtime
        return list(sorted(os.listdir(path), key=mtime))


class StatProcessor:
    logging.debug("class StatProcessor")
    utils = Utils()

    def help(self):
        command = "USAGE: stat <path/to/folder> --csv=<path/to/hash> --ts=<timestamp>"
        print(command)

    def stat(self, args):
        logging.debug("StatProcessor.stat()")
        try:
            logging.debug("args: " + args)
            valid, path_to_folder, path_to_csv, ts = self.process_args(args)
            if not valid:
                return
            self.process_stat(path_to_folder, path_to_csv, ts)
            self.utils.logs("Stat", True, "")
        except AssertionError as error:
            logging.error("StatProcessor.stat()")
            self.utils.logs("Stat", False, "Error in StatProcessor.stat(): " + str(error))

    def process_args(self, argsline):
        logging.debug("StatProcessor.process_args()")
        args_list = argsline.split()
        logging.debug("args args_list: " + str(args_list))
        if ((len(args_list) != 3) or (args_list[1].find('--csv=', 0) == -1) or (args_list[2].find('--ts=', 0) == -1)):
            self.help()
            self.utils.logs("Stat", False, "Wrong parameters")
            return 0, 0, 0, 0
        else:
            pathtofolder = args_list[0]
            pathtocsv = args_list[1]
            assert self.utils.check_folder_exists(pathtofolder) == True, \
                "process_args(): path/to/folder does not exists: " + pathtofolder
            csvpath = args_list[1].split("--csv=")[1]
            ts = self.process_timestamp_param(args_list[2])
            logging.debug("pathtofolder: " + pathtofolder + " , csvpath: " + csvpath + " , timestamp: " + str(ts))
            return 1, pathtofolder, csvpath, ts

    def process_timestamp_param(self, timestamp_param):
        assert str(timestamp_param).find('--ts=', 0) != -1, \
            "process_timestamp_param() - not valid timestamp parameter"
        ts = timestamp_param.split("--ts=")[1]
        if ts == "":
            ts = 1500000000
        return ts

    def process_stat(self, path_to_folder, path_to_csv, ts):
        logging.debug("StatProcessor.process_stat()")
        try:
            log_dir = path_to_folder + os.path.sep + "logs" + os.path.sep
            sortedlogs_all = self.utils.sorted_ls(log_dir)
            logging.debug("sortedfiles_all: " + str(sortedlogs_all))
            logs_after_ts = []
            for f in sortedlogs_all:
                if str(f).split(".")[2] > str(ts):
                    logs_after_ts.append(f)
            logging.debug("logs_after_ts: " + str(logs_after_ts))
            # analyze logs
            failed_map = {'sort': 0, 'clean': 0, 'stat': 0, 'script': 0}
            used_map = {'sort': 0, 'clean': 0, 'stat': 0, 'script': 0}
            fails_count = 0
            for file in logs_after_ts:
                file_pref = str(file).split(".")[0]
                logging.debug("file: " + file + " file_prefix: " + file_pref)
                used_map[file_pref] += 1
                file_path = log_dir + file
                f = open(file_path, "r")
                if 'Status: False' in f.read():
                    failed_map[file_pref] += 1
                    fails_count += 1
                f.close()
            most_used = least_used = most_failed = ""
            if len(logs_after_ts) > 1:
                most_used = max(used_map.items(), key=operator.itemgetter(1))[0]  # need fix, as could be more than one
                least_used = min(used_map.items(), key=operator.itemgetter(1))[0]  # need fix, as could be more than one
                if (fails_count > 0):
                    most_failed = max(failed_map.items(), key=operator.itemgetter(1))[
                        0]  # need fix, as could be more than one
            logging.debug(
                "\nmost_used: " + str(most_used) + "\nleast_used: " + str(least_used) + "\nmost_failed: " + most_failed)
            # create/write cvs file
            self.create_append_csv_stat_file(path_to_csv, most_used, least_used, most_failed)
        except AssertionError as error:
            logging.error("StatProcessor.stat()")
            self.utils.logs("Script", False, "Error in ScriptProcessor.script()")

    def create_append_csv_stat_file(self, path_to_csv, most_used, least_used, most_failed):
        try:
            ts = str(time.time()).split(".")[0]
            csv_file = path_to_csv + os.path.sep + "stat.csv"
            csv_exist = True
            if not os.path.exists(csv_file):
                header = ['Timestamp', 'Most used', 'Least used', 'Most failed']
                csv_exist = False
            f = open(csv_file, "a+")
            writer = csv.writer(f, delimiter=',')
            if not csv_exist:
                writer.writerow(header)
                os.chmod(csv_file, 0o777)
            line = [str(ts), str(most_used), str(least_used), str(most_failed)]
            writer.writerow(line)
            f.close()
        except AssertionError as error:
            logging.error("StatProcessor.create_append_csv_stat_file()")
            self.utils.logs("Script", False, "Error in ScriptProcessor.create_append_csv_stat_file(): " + str(error))


class SortProcessor:
    logging.debug("class Sort")
    utils = Utils()

    def help(self):
        command = "USAGE: sort <path/to/folder> --hash=<path/to/hash>"
        print(command)

    def sort(self, args):
        logging.debug("SortProcessor.sort()")
        try:
            logging.debug("args: " + args)
            valid, path_to_folder, path_to_hash = self.process_args(args)
            #logging.debug("path_to_folder: " + path_to_folder + ", path_to_hash: " + path_to_hash)
            if not valid:
                return
            fname = os.path.basename(path_to_hash)
            self.create_dirs(["csv", "mat", "dxl"], path_to_folder)  # create dirs for sorting files
            _, _, fileslist = next(os.walk(path_to_folder))
            logging.debug("fileslist: " + str(fileslist))
            count_csv, count_mat, count_dxl = self.sort_move_files_to_dirs(fileslist, path_to_folder)
            fname = os.path.basename(path_to_hash)
            ffullname = path_to_folder + os.path.sep + fname
            self.write_hash_file(ffullname, count_csv, count_mat, count_dxl)
            self.utils.logs("Sort", True, "", count_csv, count_mat, count_dxl)
        except AssertionError as error:
            logging.error("Error in sort_move_files_to_dirs")
            self.utils.logs("Sort", False, "Error in Sort.sort(): " + str(error))

    def process_args(self, argsline):
        logging.debug("SortProcessor.process_args()")
        args_list = argsline.split()
        logging.debug("args_list: " + str(args_list))
        if ((len(args_list) != 2) or (args_list[1].find('--hash=', 0) == -1)):
            self.help()
            self.utils.logs("Sort", False, "Wrong parameters")
            return 0,0,0
        else:
            pathtofolder = args_list[0]
            logging.debug("pathtofolder: " + pathtofolder)
            assert self.utils.check_folder_exists(pathtofolder) == True, \
                "process_args(): path/to/folder does not exists: " + pathtofolder
            hasharg = args_list[1].split("--hash=")
            hashpath = hasharg[1]
            logging.debug("hashpath: " + hashpath)
            return 1, pathtofolder, hashpath

    def create_dirs(self, list, path_to_folder):
        for dir in list:
            dir_fullpath = path_to_folder + os.path.sep + dir
            if not os.path.exists(dir_fullpath):
                os.mkdir(path_to_folder + os.path.sep + dir)

    def sort_move_files_to_dirs(self, fileslist, path_to_folder):
        count_csv = 0
        count_mat = 0
        count_dxl = 0
        try:
            owd = os.getcwd()
            os.chdir(path_to_folder)
            for f in fileslist:
                logging.debug("file: " + f)
                file_extension = os.path.splitext(f)[1]
                logging.debug("file_extension: " + file_extension)
                if file_extension == '.csv':
                    shutil.move(f, 'csv')
                    count_csv += 1
                elif file_extension == '.mat':
                    shutil.move(f, 'mat')
                    count_mat += 1
                elif file_extension == '.dxl':
                    shutil.move(f, 'dxl')
                    count_dxl += 1
            os.chdir(owd)
            return count_csv, count_mat, count_dxl
        except AssertionError:
            logging.error("Error in sort_move_files_to_dirs")

    def write_hash_file(self, ffullname, count_csv, count_mat, count_dxl):
        f = open(ffullname, "w+")
        f.write("csv: " + str(count_csv) + "\n")
        f.write("mat: " + str(count_mat) + "\n")
        f.write("dxl: " + str(count_dxl) + "\n")
        f.close()


class ScriptProcessor:
    def __init__(self, input_params):
        logging.debug("class ScriptProcessor")
        self.input_params = input_params
        self.utils = Utils()
        self.clean_processor = CleanProcessor(input_params)
        self.sort_processor = SortProcessor()
        self.stat_processor = StatProcessor()

    def help(self):
        command = "USAGE: script --script=<path/to/script>"
        print(command)

    def script(self, args):
        logging.debug("ScriptProcessor.script()")
        try:
            clean_command_args = self.input_params.get('SCRIPT')[0].get('clean_command_args')
            sort_command_args = self.input_params.get('SCRIPT')[1].get('sort_command_args')
            stat_command_args = self.input_params.get('SCRIPT')[2].get('stat_command_args')
            self.clean_processor.clean(clean_command_args)
            self.sort_processor.sort(sort_command_args)
            self.stat_processor.stat(stat_command_args)
            self.utils.logs("Script", True, "")
        except AssertionError:
            logging.error("StatProcessor.stat()")
            self.utils.logs("Script", False, "Error in ScriptProcessor.script()")


def validate_params(input_params):
    if ((not input_params.get('MAX_NUM_OF_LOG_FILES'))
            or (not input_params.get('SCRIPT'))):
        print("Missing mandatory parameter in input file")
        print("Expected json input file structure sample: ")
        print("{\n" +
              "  \"MAX_NUM_OF_LOG_FILES\": \"NUM\",\n" +
              "  \"SCRIPT\": [\n" +
              "    {\"clean_command\": \"clean .\"},\n" +
              "    {\"sort_command\": \"sort test --hash=f1\"},\n" +
              "    {\"stat_command\": \"stat . --csv=. --ts=1500000000\"}]\n" +
              "}\n")
        return False
    else:
        return True


class MiniTerminal(Cmd):
    logging.debug("class MiniTerminal")

    with open(INPUT_FILE) as json_file:
        input_params = json.load(json_file)
    assert validate_params(input_params) == True, "ERROR in Input parameters validation"

    sort_processor = SortProcessor()
    clean_processor = CleanProcessor(input_params)
    stat_processor = StatProcessor()
    script_processor = ScriptProcessor(input_params)

    prompt = 'mini> '
    intro = "Welcome! Type ? to list commands"

    def do_exit(self, inp):
        print("Bye")
        return True

    def help_exit(self):
        print('exit the application. Shorthand: x q Ctrl-D.')

    def do_sort(self, inp):
        logging.debug("do_sort")
        self.sort_processor.sort(inp)

    def help_sort(self):
        self.sort_processor.help()

    def do_clean(self, inp):
        logging.debug("do_clean")
        self.clean_processor.clean(inp)

    def help_clean(self):
        self.clean_processor.help()

    def do_stat(self, inp):
        logging.debug("do_stat")
        self.stat_processor.stat(inp)

    def help_stat(self):
        self.stat_processor.help()

    def do_script(self, inp):
        logging.debug("do_script")
        self.script_processor.script(inp)

    def help_script(self):
        self.script_processor.help()

    def default(self, inp):
        if inp == 'x' or inp == 'q':
            return self.do_exit(inp)
        print("Available commands: clean, sort, stat, script")

    do_EOF = do_exit
    help_EOF = help_exit


if __name__ == '__main__':
    main()
