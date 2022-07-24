// for standard gcc: make -f make-gcc
// for arm: make -f make-arm
#include <stdio.h>
#include <stdbool.h>
#include <curl/curl.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <syslog.h>

#define NO_ARG          0
#define OK              0
#define ERR_SETSID      1
#define SIGTERM         2
#define SIGHUP          3
#define ERR_FORK        4
#define ERR_CHDIR       5
#define ERR_WTF         9
#define INIT_ERR        10
#define REQ_ERR         11
#define NO_FILE         12
#define DAEMON_NAME     "HillandFinalDaemon"

static const char* URL = "http://52.8.135.131:8080/";
static const char* TEMP_FILENAME = "/tmp/temp";
static const char* STATE_FILENAME = "/tmp/status";

/*
 * Hilland, Joseph ECE 531 Final Assignment
 */

static void _signal_handler(const int signal) {
    switch (signal) {
        case SIGHUP:
            break;
        case SIGTERM:
            syslog(LOG_INFO, "received SIGTERM, exiting.");
            closelog();
            exit(OK);
            break;
        default:
            syslog(LOG_INFO, "received unhandled signal");
    }
}

static int send_http_request(char *message, char *type, bool verb) {
    printf("sending %s request at url: %s\n", type, URL);
    CURL *curl = curl_easy_init();
    if (curl) {
        CURLcode res;
        curl_easy_setopt(curl, CURLOPT_URL, URL);
        curl_easy_setopt(curl, CURLOPT_CUSTOMREQUEST, type);
        if (verb) {
            printf("sending message: %s\n", message);
            curl_easy_setopt(curl, CURLOPT_POSTFIELDS, message);
        } else {
            curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L);
        }
        res = curl_easy_perform(curl);

        if (res != CURLE_OK) {
            return REQ_ERR;
        }

        curl_easy_cleanup(curl);
    } else {
        return INIT_ERR;
    }
    return OK;
}

static int daemonize() {
    openlog(DAEMON_NAME, LOG_PID | LOG_NDELAY | LOG_NOWAIT, LOG_DAEMON);

    syslog(LOG_INFO, DAEMON_NAME);

    pid_t pid = fork();
    // check to see if fork was successful
    if (pid < 0) {
        syslog(LOG_ERR, strerror(pid));
        return ERR_FORK;
    }

    if (pid > 0) {
        return OK;
    }

    if (setsid() < -1) {
        syslog(LOG_ERR, strerror(pid));
        return ERR_SETSID;
    }

    close(STDIN_FILENO);
    close(STDOUT_FILENO);
    close(STDERR_FILENO);

    umask(S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH);

    if (chdir("/") < 0) {
        syslog(LOG_ERR, strerror(pid));
        return ERR_CHDIR;
    }

    signal(SIGTERM, _signal_handler);
    signal(SIGHUP, _signal_handler);

    return OK;
}

static bool file_exists(const char* filename) {
    struct stat buffer;
    return (stat(filename, &buffer) == 0) ? true : false;
}

static int read_values() {
    syslog(LOG_INFO, "Reading temp/status values.");
    // check that SIM has created files for use
    if (!file_exists(TEMP_FILENAME) || !file_exists(STATE_FILENAME) ) {
        syslog(LOG_ERR, "SIM file does not exist.");
        return NO_FILE;
    }

    File *temp_file;
    File *state_file;

    while (true) {
        syslog(LOG_INFO, "doing the work!");
        sleep(1);
    }
    return OK;
}

int main(int argc, char **argv) {

    daemonize();
    read_values();

    return OK;
}