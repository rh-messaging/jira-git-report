/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.redhat.jiragit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import static com.redhat.jiragit.LinkUtility.makeALink;
/**
 * @author Clebert Suconic
 */

public class GitParser {

   final List<String> interestingFolder = new ArrayList<>();

   String[] sourceSuffix;

   final String githubURI;

   final File folder;

   JiraParser jiraParser;

   public void setJiraPaser(JiraParser jiraParser) {
      this.jiraParser = jiraParser;
   }


   public GitParser(File folder,String githubURI) {
      this.folder = folder;
      this.githubURI = githubURI;
   }

   public GitParser addInterestingfolder(String folder) {
      interestingFolder.add(folder);
      return this;
   }

   public String[] getSourceSuffix() {
      return sourceSuffix;
   }

   public GitParser setSourceSuffix(String... sourceSuffix) {
      this.sourceSuffix = sourceSuffix;
      return this;
   }

   private String commitCell(RevCommit commit) {
      String text = commit.getId().getName().substring(0, 7);

      return makeALink(text, githubURI + "commit/" + commit.getName());
   }

   private void copy(InputStream is, OutputStream os) throws IOException {
      byte[] buffer = new byte[1024 * 4];
      int c = is.read(buffer);
      while (c >= 0) {
         os.write(buffer, 0, c);
         c = is.read(buffer);
      }
   }

   private String readString(String fileName) throws Exception {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try (InputStream in = GitParser.class.getResourceAsStream(fileName)) {
         copy(in, out);
      }

      return new String(out.toByteArray());
   }

  private void copy(String name, File directory) throws Exception {
      directory.mkdirs();
      InputStream stream = this.getClass().getResourceAsStream(name);
      File file = new File(directory, name);
      copy(stream, new FileOutputStream(file));
   }

   public void parse(File outputFile, String from, String to) throws Exception {

      PrintStream output = new PrintStream(new FileOutputStream(outputFile));

      File styleDirectory = new File(outputFile.getParent(), "styles");
      File imageDirectory = new File(outputFile.getParent(), "images");

      copy("framework.css", styleDirectory);
      copy("jquery.dataTables.min.css", styleDirectory);
      copy("jquery.dataTables.min.js", styleDirectory);
      copy("jquery.min.js", styleDirectory);

      copy("sort_both.png", imageDirectory);
      copy("sort_asc.png", imageDirectory);
      copy("sort_desc.png", imageDirectory);
      Git git = Git.open(folder);
      RevWalk walk = new RevWalk(git.getRepository());

      ObjectId fromID = git.getRepository().resolve(from); // ONE COMMIT BEFORE THE SELECTED AS WE NEED DIFFS
      ObjectId toID = git.getRepository().resolve(to);

      RevCommit fromCommit = walk.parseCommit(fromID);
      RevCommit toCommit = walk.parseCommit(toID);
      walk.markUninteresting(fromCommit);
      walk.markStart(toCommit);

      DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
      diffFormatter.setRepository(git.getRepository());

      walk.sort(RevSort.REVERSE, true);
      walk.setRevFilter(RevFilter.NO_MERGES);
      Iterator<RevCommit> commits = walk.iterator();

      ObjectReader reader = git.getRepository().newObjectReader();
      CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
      CanonicalTreeParser newTreeIter = new CanonicalTreeParser();

      output.println(readString("header.txt"));

      output.println("<body>");

      output.println("<br/>");
      output.println("<h4>Release report " + from + "(" + fromCommit.getId().getName() + ") and " + to + "(" + toCommit.getId().getName() + ")</h4>");
      output.println("<br/>");

      output.println("<table id=\"gitreport\" class=\"display\">");

      DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

      StringBuffer interestingChanges[] = new StringBuffer[interestingFolder.size()];

      output.print("<thead><tr><th>#</th><th>Commit</th><th>Date</th><th>Author</th><th>Short Message</th><th>Jira Status</th><th>Add</th><th>Rep</th><th>Del</th><th>Tot</th>");

      for (int i = 0; i < interestingFolder.size(); i++) {
         output.print("<th>" + interestingFolder.get(i) + "</th>");
      }

      output.println("</tr></thead>");

      output.println("<tbody>");

      int numberOfCommits = 0;
      while (commits.hasNext()) {
         for (int i = 0; i < interestingFolder.size(); i++) {
            // the method to cleanup a stringbuffer is cpu intensive. sorry for the extra garbage
            // this piece of code is a piece of garbage anyways :) only intended for reporting!
            interestingChanges[i] = new StringBuffer();
         }

         RevCommit commit = commits.next();

         numberOfCommits++;

         output.print("<tr>");
         output.println("<td>" + makeALink(numberOfCommits + "", githubURI + "commit/" + commit.getName()) + "</td>");
         output.print("<td>" + commitCell(commit) + " </td>");
         output.print("<td>" + dateFormat.format(commit.getAuthorIdent().getWhen()) + "</td>");
         output.print("<td>" + commit.getAuthorIdent().getName() + "</td>");
         output.print("<td>" + jiraParser.prettyCommitMessage(commit.getShortMessage()) + "</td>");

         StringBuffer bufferJIRA = new StringBuffer();
         output.println("<td>" + jiraParser.getJIRAStatus() + "</td>");

         oldTreeIter.reset(reader, commit.getParent(0).getTree());
         newTreeIter.reset(reader, commit.getTree());

         List<DiffEntry> diffList = git.diff().setOldTree(oldTreeIter).setNewTree(newTreeIter).call();

         int addition = 0, deletion = 0, replacement = 0;

         for (DiffEntry entry : diffList) {
            String path = entry.getNewPath();
            if (path.equals("/dev/null")) {
               // this could happen on deleting a whole file
               path = entry.getOldPath();
            }

            boolean interested = false;

            FileHeader header = diffFormatter.toFileHeader(entry);

            for (int i = 0; i < interestingFolder.size(); i++) {
               if (path.contains(interestingFolder.get(i)) && isSource(path)) {
                  interested = true;
                  File file = new File(path);
                  if (entry.getNewPath().equals("/dev/null")) {
                     interestingChanges[i].append(file.getName() + " "); // deleted, there's no link
                  } else {

                     int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
                     for (HunkHeader hunk : header.getHunks()) {
                        EditList edits = hunk.toEditList();
                        Iterator<Edit> editsIterator = edits.iterator();
                        while (editsIterator.hasNext()) {
                           Edit edit = editsIterator.next();
                           switch (edit.getType()) {
                              case INSERT:
                              case REPLACE:
                                 min = Math.min(min, edit.getBeginB() + 1); // the begin is always +1
                                 max = Math.max(max, edit.getEndB());
                                 break;
                              case DELETE:
                                 min = Math.min(min, edit.getBeginA() + 1); // the begin is always +1
                                 max = Math.max(max, edit.getEndA());
                                 break;
                           }
                        }
                     }

                     String fullHistory = makeALink("(+)", githubURI + "/commits/" + commit.getId().getName() + "/" + path);

                     String linkText = makeALink(file.getName(), githubURI + "blob/" + commit.getId().getName() + "/" + path + "#L" + (min) + "-L" + (max));

                     interestingChanges[i].append(linkText + fullHistory + " ");
                  }
               }
            }

            if (!interested && isSource(path)) {
               for (HunkHeader hunk : header.getHunks()) {
                  EditList edits = hunk.toEditList();
                  Iterator<Edit> editsIterator = edits.iterator();

                  if (!interested && path.endsWith(".java")) {

                     while (editsIterator.hasNext()) {
                        Edit edit = editsIterator.next();
                        switch (edit.getType()) {
                           case INSERT:
                              addition += (edit.getEndB() - edit.getBeginB());
                              break;
                           case DELETE:
                              deletion += (edit.getEndA() - edit.getBeginA());
                              break;
                           case REPLACE:
                              replacement += (edit.getEndB() - edit.getBeginB());
                              break;
                        }
                     }
                  }
                  //                  System.out.println("hunk::" + hunk);
                  //output.println("hunk:: " + hunk);
                  // System.out.println("At " + hunk.getNewStartLine(), hunk.ge)
               }
            }
         }
         output.print("<td>" + addition + "</td><td>" + replacement + "</td><td>" + deletion + "</td><td>" + (addition + replacement - deletion) + "</td>");

         for (int i = 0; i < interestingChanges.length; i++) {
            output.print("<td>" + interestingChanges[i].toString() + "</td>");
         }
         output.println("</tr>");

      }

      output.println("</tbody></table>");

      jiraParser.generateSQL(output);

      output.println("<br>Generated with <a href='https://github.com/clebertsuconic/git-release-report'> git-release-report</a>");

      output.println("</body></html>");

   }

   private boolean isSource(String path) {
      for (int i = 0; i < sourceSuffix.length; i++) {
         if (path.endsWith(sourceSuffix[i])) {
            return true;
         }
      }
      return false;
   }

   public List<String> getInterestingFolder() {
      return interestingFolder;
   }

   public File getFolder() {
      return folder;
   }

   public String getGithubURI() {
      return githubURI;
   }

   private static AbstractTreeIterator prepareTree(Git git, RevWalk walk, RevCommit commit) throws Exception {
      RevTree tree = walk.parseTree(commit.getTree().getId());

      CanonicalTreeParser treeParser = new CanonicalTreeParser();
      try (ObjectReader reader = git.getRepository().newObjectReader()) {
         treeParser.reset(reader, tree.getId());
      }

      return treeParser;
   }
}
