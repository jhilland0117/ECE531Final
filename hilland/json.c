#include <stdlib.h>

#define JSON_TOKENS 256

static jsmntok_t * json_tokenise(char *js) {
    jsmn_parser parser;
    jsmn_init(&parser);

    unsigned int n = JSON_TOKENS;
    jsmntok_t *tokens = malloc(sizeof(jsmntok_t) * n);

    int ret = jsmn_parse(&parser, js, strlen(js), tokens, n);

    while (ret == JSMN_ERROR_NOMEM)
    {
        n = n * 2 + 1;
        tokens = realloc(tokens, sizeof(jsmntok_t) * n);
        printf("tokens null\n");
        ret = jsmn_parse(&parser, js, strlen(js), tokens, n);
    }

    if (ret == JSMN_ERROR_INVAL)
        printf("jsmn_parse: invalid JSON string");
    if (ret == JSMN_ERROR_PART)
        printf("jsmn_parse: truncated JSON string");

    return tokens;
}

static int jsoneq(const char *json, jsmntok_t *tok, const char *s) {
  if (tok->type == JSMN_STRING && (int)strlen(s) == tok->end - tok->start &&
      strncmp(json + tok->start, s, tok->end - tok->start) == 0) {
    return 0;
  }
  return -1;
}

static bool json_token_streq(char *js, jsmntok_t *t, char *s) {
    return (strncmp(js + t->start, s, t->end - t->start) == 0
            && strlen(s) == (size_t) (t->end - t->start));
}

static char * json_token_tostr(char *js, jsmntok_t *t) {
    js[t->end] = '\0';
    return js + t->start;
}