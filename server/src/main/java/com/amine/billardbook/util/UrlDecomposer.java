package com.amine.billardbook.util;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;

/**
 * Classe utilitaire permettant l'analyse des URLs.<br>
 * Destinée à être utilisée par les contrôleurs et les filtres.
 */
public final class UrlDecomposer {
    /**
     * Pour valider la règle <code>HideUtilityClassConstructorCheck</code> de CheckStyle...
     * Une classe utilitaire qui n'a que des méthodes statiques ne doit pas avoir de constructeur public, parce qu'il ne servirait à rien de l'instancier.
     */
    private UrlDecomposer() {
        throw new UnsupportedOperationException();
    }

    /**
     * Renvoie un tableau de String contenant les différentes parties de l'URL (entre les slashs).<br>
     * Ne renvoie que les informations utiles. Supprime le chemin jusqu'au contexte applicatif.
     * @param request La requête HTTP à analyser
     * @return un tableau de String potentiellement vide contenant les parties de l'URL
     */
    public static String[] getUrlParts(HttpServletRequest request) {
        String effectiveUri =
                (!request.getContextPath().isEmpty())?
                request.getRequestURI().replace(request.getContextPath() + "/", ""):
                request.getRequestURI().substring(1);
        return effectiveUri.split("/");
    }

    /**
     * Renvoie un tableau de String contenant la méthode HTTP et les différentes parties de l'URL (cf. <code>GetUrlParts</code>).
     * @param request La requête HTTP à analyser
     * @return un tableau de String dont le premier élément est la méthode HTTP, et le reste les parties de l'URL
     */
    public static String[] getUrlInfo(HttpServletRequest request) {
        String[] parts = getUrlParts(request);
        String[] infos = new String[parts.length + 1];
        infos[0] = request.getMethod();
        System.arraycopy(parts, 0, infos, 1, parts.length);
        return infos;
    }

    /**
     * Renvoie la fin d'une URL, calculée à partir d'un certain terme.<br>
     * Utilisée pour les redirections en fonction d'une partie de l'URL.
     * @param request Une requête avec une URL "à rallonge"
     * @param start L'index (par rapport à un tableau renvoyé par <a href="#getUrlEnd(jakarta.servlet.http.HttpServletRequest,int)">getUrlParts()</a>) à partir duquel renvoyer l'URL
     * @return La fin de l'URL après l'élément indiqué
     */
    public static String getUrlEnd(HttpServletRequest request, int start) {
        String[] url = getUrlParts(request);
        StringBuilder urlEnd = new StringBuilder();
        for (int i = start; i < url.length; i++) {
            urlEnd.append("/").append(url[i]);
        }
        return urlEnd.toString();
    }

    /**
     * Matche la requête avec un pattern de la forme de celui renvoyé par <code>getUrlInfo</code>.
     * Renvoie <code>true</code> si les deux URLs ont la même taille et si
     * @param request Une requête dont on veut savoir si elle correspond à un pattern
     * @param pattern Un pattern qui contient la méthode HTTP et les éléments du chemin de la requête, ou "*"
     * @return True si les 2 chemins ont la même taille, et si chacun des éléments est égal à l'autre ou au wildcard "*"
     */
    public static boolean matchRequest(HttpServletRequest request, String[] pattern) {
        List<String>  requestInfo = Arrays.asList(getUrlInfo(request));
        if(requestInfo.size() != pattern.length) {
            return false;
        }
        List<String> patternAsList = Arrays.asList(pattern);
        return Arrays.stream(pattern).allMatch(element -> {
            String patternElement = requestInfo.get(patternAsList.indexOf(element));
            return patternElement.equals("*") || element.equals("*") || patternElement.equals(element);
        });
    }
}
