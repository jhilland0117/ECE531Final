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
#include <argp.h>
#include "../jsmn/jsmn.h"

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

// params, move to config file
static const char* STATE_URL = "http://52.8.135.131:8080/state";
static const char* TEMP_URL = "http://52.8.135.131:8080/temp";
static const char* REPORT_URL = "http://52.8.135.131:8080/report";
static const char* TEMP_FILENAME = "/tmp/temp";
static const char* STATE_FILENAME = "/tmp/status";

// set params for argp
const char *argp_program_version = "1.0.0.dev1";
const char *argp_program_bug_address = "jhilland@unm.edu";
static char args_doc[] = "-u http://localhost:8000 -o 'argument to pass'";
static char doc[] = "Provide a url and conduct a get, post, delete or put request.";

// arguments will be used for storing values from command line
struct Arguments {
    char *arg;  // for string argument
    char *url;    
    bool post;
    bool get;
    bool put;
    bool delete;
};

 struct Curlmem {
   char *response;
   size_t size;
 };
 
 struct Curlmem chunk = {0};
 char *KEYS[] = { "id", "temp", "time"};

// argp options required for output to user
static struct argp_option options[] = {
    {"url", 'u', "String", NO_ARG, "URL for HTTP Request, REQUIRED"},
    {"post", 'o', NO_ARG, NO_ARG, "POST HTTP Request, requires VERB"},
    {"get", 'g', NO_ARG, NO_ARG, "GET HTTP Request"},
    {"put", 'p', NO_ARG, NO_ARG, "GET HTTP Request, requires VERB"},
    {"delete", 'd', NO_ARG, NO_ARG, "GET HTTP Request, requires VERB"},
    {NO_ARG}
};

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

static size_t call_back(void *data, size_t size, size_t nmemb, void *userp) {
    size_t realsize = size * nmemb;
    struct Curlmem *mem = (struct Curlmem *)userp;

    char *ptr = realloc(mem->response, mem->size + realsize + 1);
    if(ptr == NULL) {
        return 0;
    }

    mem->response = ptr;
    memcpy(&(mem->response[mem->size]), data, realsize);
    mem->size += realsize;
    mem->response[mem->size] = 0;

    return realsize;
}

static char* send_http_request(char *url, char *message, char *type, bool verb) {
    // printf("sending %s request at url: %s\n", type, url);
    CURL *curl = curl_easy_init();
    if (curl) {
        CURLcode res;
        curl_easy_setopt(curl, CURLOPT_URL, url);
        curl_easy_setopt(curl, CURLOPT_CUSTOMREQUEST, type);
        
        if (type == "GET") {
            curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, call_back);
            curl_easy_setopt(curl, CURLOPT_WRITEDATA, (void *)&chunk);
        }

        if (verb) {
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
        return NULL;
    }
    return chunk.response;
}

static int daemonize(void) {
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

int handle_requirement_error(char* message, struct argp_state *state) {
    argp_usage(state);
    return REQ_ERR;
}

// parse command line options IF not run as a daemon instance
static error_t parse_opt(int key, char *arg, struct argp_state *state) {
    struct Arguments *arguments = state->input;
    switch (key) {
        case 'u':
            arguments->url = arg;
            break;
        case 'o':
            arguments->post = true;
            break; 
        case 'g':
            arguments->get = true;
            break;
        case 'p':
            arguments->put = true;
            break;
        case 'd':
            arguments->delete = true;
            break;
        case ARGP_KEY_NO_ARGS:
            // check if args are required based on request type, notify user
            if (arguments->post == true || arguments->put == true || arguments->delete == true) {
                return handle_requirement_error("You need to supply a VERB.\n", state);
            }
        case ARGP_KEY_ARG:
            // if too many arguments are given, notify user
            if (state->arg_num >= 1) {
                printf("Too many arguments, use quotes around your extra argument.\n");
                argp_usage(state);
                return REQ_ERR;
            }
            arguments->arg = arg;
            break;
        case ARGP_KEY_END:
            // if url is null or malformed, notify user
            if (arguments->url == NULL) {
                printf("Please provide a valid url.\n");
                argp_usage(state);
                return REQ_ERR;
            } else if (arguments->get == false && arguments->post == false && arguments->put == false && arguments->delete == false) {
                return handle_requirement_error("You must select http request type.\n", state);
            }
            break;
        case ARGP_KEY_SUCCESS:
            // perform request based on type, should this be limited to only one type allowed...
            if (arguments->get) {
                send_http_request(arguments->url, NULL, "GET", false);
                break;
            } else if (arguments->post) {
                send_http_request(arguments->url, arguments->arg, "POST", true);
                break;
            } else if (arguments->put) {
                send_http_request(arguments->url, arguments->arg, "PUT", true);
                break;
            } else if (arguments->delete) {
                send_http_request(arguments->url, arguments->arg, "DELETE", true);
                break;
            }
            break;
        default:
            return ARGP_ERR_UNKNOWN;
    }
    return OK;
}

static void read_temp(void) {
    char *buffer = NULL;
    size_t size = 0;

    /* Open your_file in read-only mode */
    FILE *fp = fopen(TEMP_FILENAME, "r");
    fseek(fp, 0, SEEK_END);
    size = ftell(fp);
    rewind(fp);
    buffer = malloc((size + 1) * sizeof(*buffer)); 
    fread(buffer, size, 1, fp);
    buffer[size] = '\0';
    
    send_http_request(REPORT_URL, buffer, "POST", true);
}

static char * json_token_tostr(char *js, jsmntok_t *t) {
    js[t->end] = '\0';
    return js + t->start;
}

jsmntok_t * json_tokenise(char *js)
{
    jsmn_parser parser;
    jsmn_init(&parser);

    unsigned int n = 256;

    jsmntok_t *tokens = malloc(sizeof(jsmntok_t) * n);

    int ret = jsmn_parse(&parser, js, strlen(js), tokens, n);

    while (ret == JSMN_ERROR_NOMEM)
    {
        n = n * 2 + 1;
        tokens = realloc(tokens, sizeof(jsmntok_t) * n);
        ret = jsmn_parse(&parser, js, strlen(js), tokens, n);
    }

    return tokens;
}

static void write_state(char *state) {
    FILE *fp = fopen(STATE_FILENAME, "w");
    fprintf(fp, state);
    fclose(fp);
}

// handle curl request to know if system should be on or off
static void handle_json(void) {
    // get commands from web server
    char* json = send_http_request(STATE_URL, NULL, "GET", false);

    jsmntok_t *tokens = json_tokenise(json);

    for (int i = 0; i < 3; i++) {
        jsmntok_t *t = &tokens[i];
        // assume root is array
        if (t->type != JSMN_ARRAY) {
            if (t->type == JSMN_STRING) {
                // printf("key: %.*s\n", t->end - t->start, json + t->start);
            } else if (t->type == JSMN_OBJECT) {
                // noop
            } else if (t->type == JSMN_PRIMITIVE) {
                // write out state to file
                char *state = json_token_tostr(json, t);
                // write_state(state);
                // printf("  * %s\n", state);
            }
        }
    }
}

static int handle_work(void) {

    // check that SIM has created files for use
    if (!file_exists(TEMP_FILENAME) || !file_exists(STATE_FILENAME) ) {
        syslog(LOG_ERR, "Simulation file does not exist.");
        return NO_FILE;
    }

    while (true) {
        // read temp and send post to webserver for thermostat
        read_temp();
        handle_json();        
        sleep(3);
    }
    
    return ERR_WTF;
}

static struct argp argp = {options, parse_opt, args_doc, doc};

int main(int argc, char **argv) {

    int err;

    if (argc > 1) {
        // default arguments, which could be done in struct
        syslog(LOG_INFO, "Using command line rather than daemon script.");
        struct Arguments arguments;
        arguments.url = NULL;
        arguments.arg = NULL;
        arguments.post = false;
        arguments.get = false;
        arguments.put = false;
        arguments.delete = false;

        // parse the arguments
        argp_parse(&argp, argc, argv, 0, 0, &arguments);
    } else {
        syslog(LOG_INFO, "Using daemon script rather than command line.");
        err = daemonize();
        if (err != OK) {
            return ERR_WTF;
        }
    }
    err = handle_work();
    if (err != OK) {
        return ERR_WTF;
    }

    return ERR_WTF;
}