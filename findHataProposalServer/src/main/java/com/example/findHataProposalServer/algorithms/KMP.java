package com.example.findHataProposalServer.algorithms;

import java.util.ArrayList;
import java.util.List;

public class KMP {

    public static List<Integer> solve(String bigText, String st) {
        String fullText = st + "#" + bigText;

        int n = fullText.length();
        int[] pi = new int[n];
        for (int i = 1; i < n; ++i) {
            int j = pi[i - 1];
            while (j > 0 && fullText.charAt(i) != fullText.charAt(j))
                j = pi[j - 1];
            if (fullText.charAt(i) == fullText.charAt(j)){
                ++j;
            }
            pi[i] = j;
        }

        List<Integer> ans = new ArrayList<>();

        for (int i = st.length() + 1; i < pi.length; i++) {
            if (pi[i] == st.length()) {
                ans.add(i);
            }
        }
        return ans;
    }

}
