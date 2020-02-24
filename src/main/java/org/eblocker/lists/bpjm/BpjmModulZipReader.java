/*
 * Copyright 2020 eBlocker Open Source UG (haftungsbeschraenkt)
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the EUPL
 * (the "License"); You may not use this work except in compliance with
 * the License. You may obtain a copy of the License at:
 *
 *   https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.eblocker.lists.bpjm;

import org.eblocker.server.icap.filter.bpjm.BpjmEntry;
import org.eblocker.server.icap.filter.bpjm.BpjmModul;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BpjmModulZipReader {

    private static final Logger log = LoggerFactory.getLogger(BpjmModulZipReader.class);

    private static final String DEPTHS_ENTRY_NAME = ".*/BPjM-Modul_\\d+_\\d+\\.txt.depth";
    private static final String DOMAINS_ENTRY_NAME = ".*/BPjM-Modul_\\d+_\\d+\\.txt.domain";
    private static final String PATHS_ENTRY_NAME = ".*/BPjM-Modul_\\d+_\\d+\\.txt.path";

    public BpjmModul read(InputStream in) throws IOException {
        ParsedBpjmZip parsedBpjmZip = readZip(in);
        validate(parsedBpjmZip);
        return map(parsedBpjmZip);
    }

    private ParsedBpjmZip readZip(InputStream in) throws IOException {
        try (ZipInputStream zipIn = new ZipInputStream(in)) {
            ParsedBpjmZip parsedBpjmZip = new ParsedBpjmZip();
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                log.debug("{} {} {} {}", entry.getName(), entry.getTime(), entry.getCompressedSize(), entry.getSize());
                parseEntry(entry, zipIn, parsedBpjmZip);
                zipIn.closeEntry();
            }
            return parsedBpjmZip;
        }
    }

    private void parseEntry(ZipEntry entry, ZipInputStream in, ParsedBpjmZip parsedBpjmZip) throws IOException {
        if (entry.isDirectory()) {
            return;
        }

        if (entry.getName().matches(DEPTHS_ENTRY_NAME)) {
            parsedBpjmZip.depths = parseEntry(entry, in, Integer::valueOf);
            parsedBpjmZip.lastModified = Math.max(parsedBpjmZip.lastModified, entry.getTime());
        } else if (entry.getName().matches(DOMAINS_ENTRY_NAME)) {
            parsedBpjmZip.domainHashes = parseEntry(entry, in, DatatypeConverter::parseHexBinary);
            parsedBpjmZip.lastModified = Math.max(parsedBpjmZip.lastModified, entry.getTime());
        } else if (entry.getName().matches(PATHS_ENTRY_NAME)) {
            parsedBpjmZip.pathHashes = parseEntry(entry, in, DatatypeConverter::parseHexBinary);
            parsedBpjmZip.lastModified = Math.max(parsedBpjmZip.lastModified, entry.getTime());
        }
    }

    private <T> List<T> parseEntry(ZipEntry entry, ZipInputStream in, Function<String, T> mapFunction) throws IOException {
        log.debug("reading entry {}", entry.getName());
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new IgnoreCloseInputStream(in)))) {
            return br.lines().map(mapFunction).collect(Collectors.toList());
        }
    }

    private void validate(ParsedBpjmZip parsedBpjmZip) throws BpjmModulZipException {
        if (parsedBpjmZip.depths == null) {
            throw new BpjmModulZipException("no depths found in bpjm archive");
        }

        if (parsedBpjmZip.domainHashes == null) {
            throw new BpjmModulZipException("no domain hashes found in bpjm archive");
        }

        if (parsedBpjmZip.pathHashes == null) {
            throw new BpjmModulZipException("no path hashes found in bpjm archive");
        }

        if (parsedBpjmZip.depths.size() != parsedBpjmZip.domainHashes.size() || parsedBpjmZip.domainHashes.size() != parsedBpjmZip.pathHashes.size()) {
            log.error("size of depths: {} domains: {}  and paths: {} differ!",
                parsedBpjmZip.depths.size(),
                parsedBpjmZip.domainHashes.size(),
                parsedBpjmZip.pathHashes.size());
            throw new BpjmModulZipException("bpjm data inconsistent");
        }
    }

    private BpjmModul map(ParsedBpjmZip parsedBpjmZip) {
        List<BpjmEntry> entries = new ArrayList<>(parsedBpjmZip.depths.size());
        for(int i = 0; i < parsedBpjmZip.depths.size(); ++i) {
            entries.add(new BpjmEntry(parsedBpjmZip.domainHashes.get(i), parsedBpjmZip.pathHashes.get(i), parsedBpjmZip.depths.get(i)));
        }
        return new BpjmModul(entries, parsedBpjmZip.lastModified);
    }

    private static class ParsedBpjmZip {
        private long lastModified;
        private List<Integer> depths;
        private List<byte[]> domainHashes;
        private List<byte[]> pathHashes;
    }

    private static class IgnoreCloseInputStream extends FilterInputStream { // NOSONAR: erroneous rule: FilterInputStream DOES OVERRIDE read(byte[], int, int)
        IgnoreCloseInputStream(ZipInputStream in) {
            super(in);
        }

        @Override
        public void close() {
            // do not delegate to wrapped stream
        }
    }
}
